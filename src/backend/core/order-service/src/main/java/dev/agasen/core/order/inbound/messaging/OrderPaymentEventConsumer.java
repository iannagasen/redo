package dev.agasen.core.order.inbound.messaging;

import dev.agasen.api.events.order.OrderCheckoutSagaEvent;
import dev.agasen.api.events.payment.PaymentEvent;
import dev.agasen.common.event.EventPublisher;
import dev.agasen.core.order.domain.OrderRepository;
import dev.agasen.core.order.domain.OrderStatus;
import dev.agasen.core.order.domain.saga.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


import static dev.agasen.core.order.application.mapper.OrderCheckoutSagaEventMapper.toCheckoutConfirmedEvent;
import static dev.agasen.core.order.application.mapper.OrderCheckoutSagaEventMapper.toCheckoutFailedEvent;

@Component
@RequiredArgsConstructor
public class OrderPaymentEventConsumer {

   private static final String PAYMENT_PARTICIPANT = "PAYMENT";

   private final EventPublisher< OrderCheckoutSagaEvent > publisher;
   private final OrderRepository orderRepository;
   private final SagaStateRepository sagaStateRepository;


   @KafkaListener( topics = { "${system.topics.checkout.payment.processed}" } )
   public void onPaymentProcessed( PaymentEvent.Processed event ) {
      var checkoutSaga = sagaStateRepository.findByOrderIdWithParticipants( event.orderId() ).orElseThrow();
      checkoutSaga.updateParticipantStatus( PAYMENT_PARTICIPANT, "SUCCESSFUL" );

      if ( checkoutSaga.hasPendingParticipants() ) return;

      var order = orderRepository.findById( event.orderId() ).orElseThrow();
      order.setStatus( OrderStatus.CONFIRMED );

      var confirmedEvent = toCheckoutConfirmedEvent( order );
      publisher.publish( confirmedEvent );
   }


   @KafkaListener( topics = { "${system.topics.checkout.payment.declined}" } )
   public void onPaymentDeclined( PaymentEvent.Declined event ) {
      var checkoutSaga = sagaStateRepository.findByOrderIdWithParticipants( event.orderId() ).orElseThrow();
      checkoutSaga.updateParticipantStatus( PAYMENT_PARTICIPANT, "FAILED" );

      if ( checkoutSaga.hasPendingParticipants() ) return;

      var order = orderRepository.findById( event.orderId() ).orElseThrow();
      order.setStatus( OrderStatus.PAYMENT_FAILED );

      var checkoutFailedEvent = toCheckoutFailedEvent( order, event.message() );
      publisher.publish( checkoutFailedEvent );
   }

   @KafkaListener( topics = { "${system.topics.checkout.payment.refunded}" } )
   public void onPaymentDeclined( PaymentEvent.Refunded event ) {

   }

}
