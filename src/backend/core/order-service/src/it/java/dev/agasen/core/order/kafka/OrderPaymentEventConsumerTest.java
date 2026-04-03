package dev.agasen.core.order.kafka;

import dev.agasen.api.events.order.OrderCheckoutSagaEvent;
import dev.agasen.api.events.payment.PaymentEvent;
import dev.agasen.core.order.domain.Order;
import dev.agasen.core.order.domain.OrderRepository;
import dev.agasen.core.order.domain.OrderStatus;
import dev.agasen.core.order.domain.saga.SagaParticipant;
import dev.agasen.core.order.domain.saga.SagaState;
import dev.agasen.core.order.domain.saga.SagaStateRepository;
import dev.agasen.core.order.outbound.messaging.OrderCheckoutSagaEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link dev.agasen.core.order.inbound.messaging.OrderPaymentEventConsumer}.
 *
 * <p>Each test sends a real Kafka message via {@code @EmbeddedKafka} and verifies the
 * consumer's effect on the order status and the downstream saga event published.
 *
 * <p>Infrastructure:
 * <ul>
 *   <li>{@code @EmbeddedKafka} — in-process broker; no external Kafka needed.</li>
 *   <li>{@code @MockitoBean OrderCheckoutSagaEventPublisher} — captures published saga events
 *       and prevents real Kafka publishing on the outbound side.</li>
 *   <li>{@code @MockitoBean OrderRepository / SagaStateRepository} — control test data
 *       without touching a real database.</li>
 * </ul>
 *
 * <p>Scenarios covered:
 * <ol>
 *   <li>Payment processed, single participant → order CONFIRMED + Confirmed event published.</li>
 *   <li>Payment processed, second participant still pending → saga waits, no event published.</li>
 *   <li>Payment declined, single participant → order PAYMENT_FAILED + Failed event published.</li>
 *   <li>Payment declined, second participant still pending → saga waits, no event published.</li>
 * </ol>
 */
@SpringBootTest
@ActiveProfiles( "test" )
@EmbeddedKafka(
   partitions = 1,
   topics = {
      "checkout.payment.processed",
      "checkout.payment.declined",
      "checkout.payment.refunded"
   },
   brokerProperties = {
      "transaction.state.log.replication.factor=1",
      "transaction.state.log.min.isr=1"
   }
)
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
class OrderPaymentEventConsumerTest {

   // Topic constants match kafka.yml system.topics.checkout.payment.*
   private static final String PAYMENT_PROCESSED_TOPIC = "checkout.payment.processed";
   private static final String PAYMENT_DECLINED_TOPIC  = "checkout.payment.declined";

   @Autowired private KafkaTemplate< String, Object > kafkaTemplate;

   // Mock the concrete publisher so both OrderPaymentEventConsumer (via EventPublisher<>)
   // and OrderCheckoutSagaInitiator (via concrete type) receive the same mock.
   @MockitoBean private OrderCheckoutSagaEventPublisher publisher;

   @MockitoBean private OrderRepository orderRepository;
   @MockitoBean private SagaStateRepository sagaStateRepository;

   // ── 1. Payment processed — all participants satisfied ─────────────────────

   /**
    * Happy path: payment succeeds and PAYMENT is the only participant.
    * <p>
    * After {@code updateParticipantStatus("PAYMENT", "SUCCESSFUL")}, no participants
    * remain pending, so the consumer sets the order to CONFIRMED and fires
    * {@link OrderCheckoutSagaEvent.Confirmed}.
    */
   @Test
   void onPaymentProcessed_singleParticipant_confirmsOrderAndPublishesConfirmedEvent() throws Exception {
      Long orderId = 1L;

      SagaState sagaState = sagaStateWith( orderId, participant( "PAYMENT", true, "PENDING" ) );
      Order order = orderWithStatus( OrderStatus.PENDING );

      when( sagaStateRepository.findByOrderIdWithParticipants( orderId ) ).thenReturn( Optional.of( sagaState ) );
      when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );

      kafkaTemplate.send( PAYMENT_PROCESSED_TOPIC, String.valueOf( orderId ),
         PaymentEvent.Processed.builder()
            .eventId( UUID.randomUUID() )
            .occurredAt( Instant.now() )
            .orderId( orderId )
            .build()
      ).get();

      TimeUnit.SECONDS.sleep( 3 );

      assertThat( order.getStatus() ).isEqualTo( OrderStatus.CONFIRMED );

      ArgumentCaptor< OrderCheckoutSagaEvent > captor = ArgumentCaptor.forClass( OrderCheckoutSagaEvent.class );
      verify( publisher ).publish( captor.capture() );
      assertThat( captor.getValue() ).isInstanceOf( OrderCheckoutSagaEvent.Confirmed.class );
      assertThat( ( (OrderCheckoutSagaEvent.Confirmed) captor.getValue() ).orderId() ).isEqualTo( orderId );
   }

   // ── 2. Payment processed — second participant still pending ───────────────

   /**
    * Choreography hold: payment succeeds but the CART participant has not reported yet.
    * <p>
    * {@code hasPendingParticipants()} returns {@code true} after the PAYMENT update,
    * so the consumer returns early — no order status change and no saga event.
    */
   @Test
   void onPaymentProcessed_cartParticipantStillPending_sagaWaitsAndPublishesNothing() throws Exception {
      Long orderId = 2L;

      SagaState sagaState = sagaStateWith( orderId,
         participant( "PAYMENT", true,  "PENDING" ),
         participant( "CART",    false, "PENDING" )
      );

      when( sagaStateRepository.findByOrderIdWithParticipants( orderId ) ).thenReturn( Optional.of( sagaState ) );

      kafkaTemplate.send( PAYMENT_PROCESSED_TOPIC, String.valueOf( orderId ),
         PaymentEvent.Processed.builder()
            .eventId( UUID.randomUUID() )
            .occurredAt( Instant.now() )
            .orderId( orderId )
            .build()
      ).get();

      TimeUnit.SECONDS.sleep( 3 );

      verify( publisher, never() ).publish( any() );
      verify( orderRepository, never() ).findById( any() );
   }

   // ── 3. Payment declined — all participants resolved ───────────────────────

   /**
    * Failure path: payment is declined and PAYMENT is the only participant.
    * <p>
    * The consumer marks the order as PAYMENT_FAILED and fires
    * {@link OrderCheckoutSagaEvent.Failed} with the decline message.
    */
   @Test
   void onPaymentDeclined_singleParticipant_setsPaymentFailedAndPublishesFailedEvent() throws Exception {
      Long orderId = 3L;
      String declineReason = "Insufficient funds";

      SagaState sagaState = sagaStateWith( orderId, participant( "PAYMENT", true, "PENDING" ) );
      Order order = orderWithStatus( OrderStatus.PENDING );

      when( sagaStateRepository.findByOrderIdWithParticipants( orderId ) ).thenReturn( Optional.of( sagaState ) );
      when( orderRepository.findById( orderId ) ).thenReturn( Optional.of( order ) );

      kafkaTemplate.send( PAYMENT_DECLINED_TOPIC, String.valueOf( orderId ),
         PaymentEvent.Declined.builder()
            .eventId( UUID.randomUUID() )
            .occurredAt( Instant.now() )
            .orderId( orderId )
            .message( declineReason )
            .build()
      ).get();

      TimeUnit.SECONDS.sleep( 3 );

      assertThat( order.getStatus() ).isEqualTo( OrderStatus.PAYMENT_FAILED );

      ArgumentCaptor< OrderCheckoutSagaEvent > captor = ArgumentCaptor.forClass( OrderCheckoutSagaEvent.class );
      verify( publisher ).publish( captor.capture() );
      assertThat( captor.getValue() ).isInstanceOf( OrderCheckoutSagaEvent.Failed.class );
      OrderCheckoutSagaEvent.Failed failed = (OrderCheckoutSagaEvent.Failed) captor.getValue();
      assertThat( failed.orderId() ).isEqualTo( orderId );
      assertThat( failed.errorMessage() ).isEqualTo( declineReason );
   }

   // ── 4. Payment declined — second participant still pending ────────────────

   /**
    * Choreography hold: payment is declined but the CART participant has not reported yet.
    * <p>
    * {@code hasPendingParticipants()} returns {@code true}, so the consumer returns
    * early — no order update and no saga event.
    */
   @Test
   void onPaymentDeclined_cartParticipantStillPending_sagaWaitsAndPublishesNothing() throws Exception {
      Long orderId = 4L;

      SagaState sagaState = sagaStateWith( orderId,
         participant( "PAYMENT", true,  "PENDING" ),
         participant( "CART",    false, "PENDING" )
      );

      when( sagaStateRepository.findByOrderIdWithParticipants( orderId ) ).thenReturn( Optional.of( sagaState ) );

      kafkaTemplate.send( PAYMENT_DECLINED_TOPIC, String.valueOf( orderId ),
         PaymentEvent.Declined.builder()
            .eventId( UUID.randomUUID() )
            .occurredAt( Instant.now() )
            .orderId( orderId )
            .message( "Card expired" )
            .build()
      ).get();

      TimeUnit.SECONDS.sleep( 3 );

      verify( publisher, never() ).publish( any() );
      verify( orderRepository, never() ).findById( any() );
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private SagaState sagaStateWith( Long orderId, SagaParticipant... participants ) {
      SagaState state = new SagaState();
      state.setSagaId( UUID.randomUUID() );
      state.setSagaType( "ORDER_CHECKOUT" );
      state.setOrderId( orderId );
      state.setStatus( "RUNNING" );
      for ( SagaParticipant p : participants ) {
         state.getParticipants().add( p );
      }
      return state;
   }

   private SagaParticipant participant( String name, boolean required, String status ) {
      SagaParticipant p = new SagaParticipant();
      p.setParticipant( name );
      p.setRequired( required );
      p.setStatus( status );
      return p;
   }

   private Order orderWithStatus( OrderStatus status ) {
      Order order = new Order();
      order.setStatus( status );
      return order;
   }
}
