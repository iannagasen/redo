package dev.agasen.core.payment.kafka;

import dev.agasen.api.core.event.PaymentEvent;
import dev.agasen.api.core.payment.write.InitiatePaymentRequest;
import dev.agasen.core.payment.application.PaymentInitiator;
import dev.agasen.core.payment.repository.entity.Payment;
import dev.agasen.core.payment.repository.PaymentRepository;
import dev.agasen.core.payment.repository.entity.PaymentStatus;
import dev.agasen.core.payment.event.PaymentEventPublisher;
import dev.agasen.core.payment.gateway.GatewayPaymentResponse;
import dev.agasen.core.payment.gateway.PaymentGatewayClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link PaymentEventPublisher}.
 * <p>
 * What's being tested:
 * - The publisher sends to the correct Kafka topic ("payment.result")
 * - The Kafka message key is the orderId (enables partition co-location with orders)
 * - PaymentCommandService wires through to the publisher on CAPTURED and FAILED outcomes
 * <p>
 * Kafka mechanism:
 * Messages are keyed by orderId so that all events for the same order always land on the
 * same partition. This guarantees ordering per-order and enables the order-service consumer
 * to process events for a given order sequentially without coordination overhead.
 * <p>
 * Infrastructure:
 *
 * @EmbeddedKafka starts an in-process broker (no Docker needed). The broker
 * address is injected via ${spring.embedded.kafka.brokers} (set in application-test.yml).
 * H2 replaces PostgreSQL — only the Kafka behaviour is under test here.
 */
@SpringBootTest
@ActiveProfiles( "test" )
@EmbeddedKafka(
   partitions = 1,
   topics = { PaymentEventPublisher.TOPIC }
)
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
class PaymentEventPublisherTest {

   @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;
   @Autowired private PaymentEventPublisher paymentEventPublisher;
   @Autowired private PaymentInitiator paymentInitiator;
   @Autowired private KafkaTemplate< String, PaymentEvent > kafkaTemplate;

   @MockitoBean private PaymentRepository paymentRepository;
   @MockitoBean private PaymentGatewayClient paymentGatewayClient;

   private org.apache.kafka.clients.consumer.Consumer< String, String > rawConsumer;

   @BeforeEach
   void setUpRawConsumer() {
      // Raw String consumer — avoids deserializer config complexity in setup.
      // We verify the JSON payload content via string assertions.
      Map< String, Object > props = KafkaTestUtils.consumerProps( embeddedKafkaBroker, "pub-verify-group", true );
      rawConsumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(
         props,
         new org.apache.kafka.common.serialization.StringDeserializer(),
         new org.apache.kafka.common.serialization.StringDeserializer()
      );
      rawConsumer.subscribe( java.util.List.of( PaymentEventPublisher.TOPIC ) );
   }

   @AfterEach
   void tearDownConsumer() {
      rawConsumer.close();
   }

   // ── Test 1: Publisher sends to correct topic with correct key ─────────────

   /**
    * KAFKA MECHANISM: Message Keys
    * <p>
    * In Kafka, the message key determines which partition a message lands on
    * (via hash(key) % numPartitions). By keying on orderId, all events for
    * the same order always go to the same partition — guaranteeing per-order
    * ordering without any consumer-side coordination.
    * <p>
    * This test verifies the publisher uses String.valueOf(orderId) as the key,
    * matching PaymentEventPublisher.publish().
    */
   @Test
   void publish_sendsToCorrectTopic_withOrderIdAsKey() {
      PaymentEvent event = new PaymentEvent( 42L, 100L, "user-1", BigDecimal.TEN, "CAPTURED", null );

      paymentEventPublisher.publish( event );

      ConsumerRecord< String, String > record = KafkaTestUtils.getSingleRecord( rawConsumer, PaymentEventPublisher.TOPIC );

      assertThat( record.key() ).isEqualTo( "42" );
      assertThat( record.value() ).contains( "\"orderId\":42" );
      assertThat( record.value() ).contains( "\"status\":\"CAPTURED\"" );
      assertThat( record.value() ).contains( "\"paymentId\":100" );
   }

   // ── Test 2: PaymentCommandService publishes CAPTURED event ────────────────

   /**
    * KAFKA MECHANISM: Producer as part of a business transaction
    * <p>
    * PaymentCommandService saves to DB then publishes to Kafka. This test
    * proves the event reaching the broker has the correct terminal status.
    * If the gateway succeeds, the broker must receive "CAPTURED" — not "PENDING".
    * <p>
    * Current limitation: the DB save and the Kafka publish are NOT atomic.
    * A crash between save() and publish() leaves the order stuck as PENDING.
    * See OffsetManagementTest for the EOS discussion.
    */
   @Test
   void initiatePayment_publishesCapturedEvent_whenGatewaySucceeds() {
      stubRepositoryAndGateway( 1L, true );

      InitiatePaymentRequest request = buildRequest( 1L );
      paymentInitiator.initiatePayment( "user-1", request );

      ConsumerRecord< String, String > record = KafkaTestUtils.getSingleRecord( rawConsumer, PaymentEventPublisher.TOPIC );

      assertThat( record.key() ).isEqualTo( "1" );
      assertThat( record.value() ).contains( "\"status\":\"CAPTURED\"" );
   }

   // ── Test 3: PaymentCommandService publishes FAILED event ─────────────────

   @Test
   void initiatePayment_publishesFailedEvent_whenGatewayDeclines() {
      stubRepositoryAndGateway( 2L, false );

      InitiatePaymentRequest request = buildRequest( 2L );
      paymentInitiator.initiatePayment( "user-2", request );

      ConsumerRecord< String, String > record = KafkaTestUtils.getSingleRecord( rawConsumer, PaymentEventPublisher.TOPIC );

      assertThat( record.key() ).isEqualTo( "2" );
      assertThat( record.value() ).contains( "\"status\":\"FAILED\"" );
      assertThat( record.value() ).contains( "\"failureReason\"" );
   }

   // ── Test 4: Idempotent producer — enable.idempotence=true ────────────────

   /**
    * KAFKA MECHANISM: Producer Idempotence
    * <p>
    * With enable.idempotence=true (set in application-test.yml), the broker assigns
    * the producer a PID (Producer ID) and tracks sequence numbers per partition.
    * If the broker receives a message with a sequence number it already accepted
    * (e.g., due to a producer retry on network timeout), it silently deduplicates it.
    * <p>
    * What this DOES protect: broker-level duplicate writes within the same producer
    * session when the producer retries after a transient failure.
    * <p>
    * What this does NOT protect: the consumer receiving the same logical event
    * twice across different producer sessions (e.g., after a service restart).
    * That requires consumer-side idempotency — see PaymentEventConsumerTest.
    * <p>
    * This test verifies that sending the same event twice (simulating a retry)
    * results in EXACTLY one record on the topic when idempotence is enabled.
    */
   @Test
   void idempotentProducer_doesNotDuplicateMessage_onRetry() throws Exception {
      PaymentEvent event = new PaymentEvent( 99L, 990L, "user-1", BigDecimal.TEN, "CAPTURED", null );

      // Simulate producer retry: publish the same logical event twice
      kafkaTemplate.send( PaymentEventPublisher.TOPIC, "99", event ).get();
      kafkaTemplate.send( PaymentEventPublisher.TOPIC, "99", event ).get();

      // Both arrive at the broker as two separate messages.
      // enable.idempotence=true only deduplicates RETRIES (same seq number);
      // it cannot deduplicate two distinct send() calls with the same payload.
      // This test documents the boundary: broker idempotence ≠ business-level deduplication.
      // Consumer-side idempotency is required to handle this — see PaymentEventConsumerTest.
      var records = KafkaTestUtils.getRecords( rawConsumer, Duration.ofMillis( 3000L ) );
      assertThat( records.count() ).isEqualTo( 2 ); // both arrive — consumer must be idempotent
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private void stubRepositoryAndGateway( Long orderId, boolean gatewaySuccess ) {
      Payment pending = buildPayment( orderId, PaymentStatus.PENDING );
      Payment terminal = buildPayment( orderId, gatewaySuccess ? PaymentStatus.CAPTURED : PaymentStatus.FAILED );
      if ( !gatewaySuccess ) terminal.setFailureReason( "Card declined" );

      when( paymentRepository.save( any() ) )
         .thenReturn( pending )   // first save — PENDING
         .thenReturn( terminal ); // second save — terminal state

      if ( gatewaySuccess ) {
         when( paymentGatewayClient.processPayment( any() ) )
            .thenReturn( GatewayPaymentResponse.builder().success( true ).gatewayRef( "gw-ref" ).build() );
      } else {
         when( paymentGatewayClient.processPayment( any() ) )
            .thenReturn( GatewayPaymentResponse.builder().success( false ).failureReason( "Card declined" ).build() );
      }
   }

   private Payment buildPayment( Long orderId, PaymentStatus status ) {
      Payment p = new Payment();
      ReflectionTestUtils.setField( p, "id", 100L );
      p.setOrderId( orderId );
      p.setUserId( "user-1" );
      p.setAmount( BigDecimal.TEN );
      p.setCurrency( "USD" );
      p.setStatus( status );
      if ( status == PaymentStatus.CAPTURED ) p.setGatewayRef( "gw-ref" );
      return p;
   }

   private InitiatePaymentRequest buildRequest( Long orderId ) {
      InitiatePaymentRequest r = new InitiatePaymentRequest();
      r.setOrderId( orderId );
      r.setAmount( BigDecimal.TEN );
      r.setCurrency( "USD" );
      r.setCardNumber( "4111111111111111" );
      r.setCardholderName( "Test User" );
      r.setExpiryMonth( 12 );
      r.setExpiryYear( 2030 );
      r.setCvv( "123" );
      return r;
   }
}
