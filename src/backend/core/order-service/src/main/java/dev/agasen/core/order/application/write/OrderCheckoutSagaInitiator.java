package dev.agasen.core.order.application.write;

import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.api.core.order.write.CheckoutRequest;
import dev.agasen.api.events.order.OrderCheckoutSagaEvent;
import dev.agasen.core.order.domain.saga.SagaParticipant;
import dev.agasen.core.order.domain.saga.SagaState;
import dev.agasen.core.order.domain.saga.SagaStateRepository;
import dev.agasen.core.order.outbound.messaging.OrderCheckoutSagaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Saga entry point for the checkout flow.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Persist the order (local transaction)</li>
 *   <li>Open a saga state row with the expected participants</li>
 *   <li>Fire {@link OrderCheckoutSagaEvent.Created} to start the saga</li>
 * </ol>
 *
 * <p>Nothing else. Order-service does not know what happens next.
 * Payment-service and cart-service will react to {@link OrderCheckoutSagaEvent.Created} independently.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderCheckoutSagaInitiator {

   private static final String SAGA_TYPE = "ORDER_CHECKOUT";

   /**
    * PAYMENT blocks order confirmation. CART is best-effort.
    */
   private static final List< ParticipantSpec > PARTICIPANTS = List.of(
      new ParticipantSpec( "PAYMENT", true ),
      new ParticipantSpec( "CART", false )
   );

   private final OrderCheckoutSagaEventPublisher eventPublisher;
   private final OrderCreationService orderCreationService;

   public OrderDetails checkout( CheckoutRequest request ) {
      var orderDetails = orderCreationService.createOrder( request.orderDetails() );
      initiate( orderDetails.getId(), SAGA_TYPE, PARTICIPANTS );
      eventPublisher.publish( toEvent( orderDetails, request ) );
      return orderDetails;
   }

   private OrderCheckoutSagaEvent.Created toEvent( OrderDetails orderDetails, CheckoutRequest request ) {
      var payment = request.paymentDetails();
      return OrderCheckoutSagaEvent.Created.builder()
         .eventId( UUID.randomUUID() )
         .occurredAt( Instant.now() )
         .orderId( orderDetails.getId() )
         .userId( orderDetails.getUserId() )
         .items( request.orderDetails().getItems() )
         .total( orderDetails.getTotal() )
         .cardNumber( payment.getCardNumber() )
         .cardholderName( payment.getCardholderName() )
         .expiryMonth( payment.getExpiryMonth() )
         .expiryYear( payment.getExpiryYear() )
         .cvv( payment.getCvv() )
         .build();
   }

   public record ParticipantSpec( String name, boolean required ) {}

   private static final String RUNNING = "RUNNING";
   private static final String COMPLETED = "COMPLETED";
   private static final String FAILED = "FAILED";
   private static final String PENDING = "PENDING";
   private static final String SUCCESS = "SUCCESS";

   private final SagaStateRepository sagaStateRepository;

   /**
    * Creates a new saga state row and one participant row per spec.
    * Called by the saga entry point when the order is first persisted.
    */
   public SagaState initiate( Long orderId, String sagaType, List< ParticipantSpec > specs ) {
      SagaState state = new SagaState();
      state.setSagaId( UUID.randomUUID() );
      state.setOrderId( orderId );
      state.setSagaType( sagaType );
      state.setStatus( RUNNING );

      specs.forEach( spec -> {
         SagaParticipant p = new SagaParticipant();
         p.setSagaState( state );
         p.setParticipant( spec.name() );
         p.setRequired( spec.required() );
         p.setStatus( PENDING );
         state.getParticipants().add( p );
      } );

      SagaState saved = sagaStateRepository.save( state );
      log.info( "Saga initiated: sagaId={}, orderId={}, type={}", saved.getSagaId(), orderId, sagaType );
      return saved;
   }
}
