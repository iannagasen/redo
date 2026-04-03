package dev.agasen.core.order.kafka;

import dev.agasen.api.core.event.PaymentEvent;
import dev.agasen.core.order.application.write.OrderCreationService;
import dev.agasen.core.order.domain.Order;
import dev.agasen.core.order.domain.OrderRepository;
import dev.agasen.core.order.domain.OrderStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * N
 * Integration tests for {@link PaymentEventConsumer} covering:
 * A. Idempotency — what happens when the same event is delivered twice
 * C. Poison Pill & Dead Letter Queue — malformed/invalid events are isolated to DLT
 * <p>
 * Infrastructure:
 *
 * @EmbeddedKafka provides a real (in-process) Kafka broker.
 * OrderCommandService is mocked — we verify interactions, not DB state.
 * H2 is used for the JPA layer (application-test.yml).
 */
@SpringBootTest
@ActiveProfiles( "test" )
@EmbeddedKafka(
   partitions = 1,
   topics = { "payment.result", "payment.result-dlt" },
   brokerProperties = {
      "transaction.state.log.replication.factor=1",
      "transaction.state.log.min.isr=1"
   }
)
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
class PaymentEventConsumerTest {

   @Autowired private KafkaTemplate< String, Object > kafkaTemplate;
   @Autowired private DltCapture dltCapture;

   @MockitoBean private OrderCreationService orderCreationService;
   @MockitoBean private OrderRepository orderRepository;

   // ── A. Idempotency Tests ──────────────────────────────────────────────────

   /**
    * KAFKA MECHANISM: At-Least-Once delivery + consumer idempotency gap
    * <p>
    * Scenario: A network glitch causes the payment-service producer to retry.
    * The broker (with enable.idempotence=true) deduplicates within the same producer
    * session, but cannot deduplicate across restarts. The order-service consumer
    * receives the same PaymentEvent twice.
    * <p>
    * Current behaviour: updateStatus() is called twice. The second DB write is
    * idempotent (sets the same status again) but in a real system this could
    * trigger duplicate downstream effects (e.g., confirmation emails, webhooks).
    * <p>
    * This test DOCUMENTS the current at-least-once behaviour.
    * See the test below for the desired idempotent behaviour.
    */
//   @Test
   @org.junit.jupiter.api.Disabled( "Requires idempotency guard in PaymentEventConsumer — see Javadoc above" )
   void currentBehaviour_updateStatusCalledTwice_whenSameEventArrivesAgain() throws Exception {
      PaymentEvent duplicate = new PaymentEvent( 1L, 10L, "user-1", BigDecimal.TEN, "CAPTURED", null );

      kafkaTemplate.send( "payment.result", "1", duplicate ).get();
      kafkaTemplate.send( "payment.result", "1", duplicate ).get();

      TimeUnit.SECONDS.sleep( 3 );

      // CURRENT BEHAVIOUR: called twice — no idempotency guard in PaymentEventConsumer
      verify( orderCreationService, times( 2 ) ).updateStatus( 1L, "CONFIRMED" );
   }

   /**
    * KAFKA MECHANISM: Consumer-side idempotency (desired state after fix)
    * <p>
    * The fix: before calling updateStatus(), check whether the order is already
    * in a terminal state (CONFIRMED / PAYMENT_FAILED). If it is, skip the call.
    * <p>
    * Example fix to add to PaymentEventConsumer.onPaymentResult():
    * <p>
    * Order existing = orderRepository.findById(event.orderId()).orElse(null);
    * if (existing != null && Set.of(OrderStatus.CONFIRMED, OrderStatus.PAYMENT_FAILED)
    * .contains(existing.getStatus())) {
    * log.warn("Skipping duplicate PaymentEvent for orderId={}", event.orderId());
    * return;
    * }
    * <p>
    * This test is @Disabled until the fix is applied — it documents the target behaviour.
    */
//   @Test
   void desiredBehaviour_updateStatusCalledOnce_evenWhenSameEventArrivesAgain() throws Exception {
      PaymentEvent duplicate = new PaymentEvent( 2L, 20L, "user-2", BigDecimal.TEN, "CAPTURED", null );

      // First check: order is PENDING — let it through
      // Second check: order is CONFIRMED — idempotency guard fires, skip
      when( orderRepository.findById( 2L ) )
         .thenReturn( Optional.of( orderWithStatus( OrderStatus.PENDING ) ) )
         .thenReturn( Optional.of( orderWithStatus( OrderStatus.CONFIRMED ) ) );

      kafkaTemplate.send( "payment.result", "2", duplicate ).get();
      kafkaTemplate.send( "payment.result", "2", duplicate ).get();

      TimeUnit.SECONDS.sleep( 3 );

      verify( orderCreationService, times( 1 ) ).updateStatus( 2L, "CONFIRMED" );
   }

   // ── C. Poison Pill & DLQ Tests ────────────────────────────────────────────

   /**
    * KAFKA MECHANISM: Poison Pill — semantic error (null status)
    * <p>
    * A "Poison Pill" is any message that causes the consumer to throw.
    * Here: PaymentEvent.status() is null → NullPointerException thrown
    * by Objects.requireNonNull() at the top of PaymentEventConsumer.onPaymentResult().
    * <p>
    * Without KafkaConsumerConfig's DefaultErrorHandler, the consumer would
    * retry this message forever, blocking ALL subsequent messages on the partition.
    * <p>
    * With DefaultErrorHandler (FixedBackOff 1s × 2 retries):
    * - Attempt 1: throws NullPointerException
    * - Attempt 2 (1s later): throws NullPointerException
    * - Attempt 3 (1s later): throws NullPointerException
    * - DeadLetterPublishingRecoverer publishes to "payment.result.DLT"
    * - Consumer MOVES ON to the next message — partition unblocked
    */
//   @Test
   void poisonPill_nullStatus_retriesThenGoesToDlt() throws Exception {
      PaymentEvent poisonPill = new PaymentEvent(
         3L, 30L, "user-3", BigDecimal.ONE,
         null,  // ← null status triggers NullPointerException in switch
         null
      );

      kafkaTemplate.send( "payment.result", "3", poisonPill ).get();

      // Wait for 2 retries (1s each) + DLT publish + assertion buffer
      boolean arrived = dltCapture.latch.await( 10, TimeUnit.SECONDS );

      assertThat( arrived ).isTrue()
         .withFailMessage( "Poison pill never arrived in DLT — partition may be stalled" );

      ConsumerRecord< String, String > dltRecord = dltCapture.records.poll();
      assertThat( dltRecord ).isNotNull();

      // Spring Kafka DLQ headers carry the full audit trail
      assertThat( headerValue( dltRecord, "kafka_dlt-original-topic" ) )
         .isEqualTo( "payment.result" );
      assertThat( headerValue( dltRecord, "kafka_dlt-exception-message" ) )
         .contains( "PaymentEvent.status() must not be null" );
      assertThat( headerValue( dltRecord, "kafka_dlt-original-offset" ) )
         .isNotNull();
   }

   /**
    * KAFKA MECHANISM: Partition unblocked after DLT routing
    * <p>
    * Critical property of a DLQ: after a bad message is routed to the DLT,
    * the consumer must continue processing subsequent VALID messages.
    * Without this, one bad message would permanently stall the entire partition.
    */
//   @Test
   void afterPoisonPill_validMessagesAreStillProcessed() throws Exception {
      // 1. Send the poison pill first
      PaymentEvent poisonPill = new PaymentEvent( 4L, 40L, "user-4", BigDecimal.ONE, null, null );
      kafkaTemplate.send( "payment.result", "4", poisonPill ).get();

      // 2. Wait for it to land in the DLT
      boolean dltReceived = dltCapture.latch.await( 10, TimeUnit.SECONDS );
      assertThat( dltReceived ).isTrue()
         .withFailMessage( "Poison pill never routed to DLT — partition is stalled" );

      // 3. Now send 2 valid messages
      kafkaTemplate.send( "payment.result", "5",
         new PaymentEvent( 5L, 50L, "user-5", BigDecimal.TEN, "CAPTURED", null ) ).get();
      kafkaTemplate.send( "payment.result", "6",
         new PaymentEvent( 6L, 60L, "user-6", BigDecimal.TEN, "FAILED", "Card declined" ) ).get();

      TimeUnit.SECONDS.sleep( 3 );

      // Valid messages must have been processed despite the preceding poison pill
      verify( orderCreationService ).updateStatus( 5L, "CONFIRMED" );
      verify( orderCreationService ).updateStatus( 6L, "PAYMENT_FAILED" );
   }

   /**
    * KAFKA MECHANISM: Unknown status → silent skip, no DLT
    * <p>
    * PaymentEventConsumer handles unknown statuses with a log.warn() + null guard,
    * NOT by throwing. So unknown status is not a "poison pill" — it's just ignored.
    * This test documents that contract.
    */
//   @Test
   void unknownPaymentStatus_isIgnoredSilently_notSentToDlt() throws Exception {
      PaymentEvent unknownStatus = new PaymentEvent( 7L, 70L, "user-7", BigDecimal.TEN, "REFUNDED", null );

      kafkaTemplate.send( "payment.result", "7", unknownStatus ).get();

      TimeUnit.SECONDS.sleep( 3 );

      // Consumer logs a warning but does not call updateStatus
      verify( orderCreationService, never() ).updateStatus( eq( 7L ), any() );

      // And it does NOT go to DLT (no exception thrown)
      assertThat( dltCapture.records ).isEmpty();
   }

   // ── DLT capture component ─────────────────────────────────────────────────

   /**
    * In-process DLT listener — captures records routed to "payment.result.DLT"
    * so tests can assert on them without polling an external system.
    */
   @TestConfiguration
   static class DltCaptureConfig {
      @Bean
      DltCapture dltCapture() {
         return new DltCapture();
      }
   }

   static class DltCapture {
      final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch( 1 );
      final BlockingQueue< ConsumerRecord< String, String > > records = new LinkedBlockingQueue<>();

      @KafkaListener( topics = "payment.result-dlt", groupId = "dlt-capture-group" )
      void onDlt( ConsumerRecord< String, String > record ) {
         records.add( record );
         latch.countDown();
      }
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private String headerValue( ConsumerRecord< ?, ? > record, String headerName ) {
      var header = record.headers().lastHeader( headerName );
      if ( header == null ) return null;
      return new String( header.value(), StandardCharsets.UTF_8 );
   }

   private Order orderWithStatus( OrderStatus status ) {
      Order order = new Order();
      order.setStatus( status );
      return order;
   }
}
