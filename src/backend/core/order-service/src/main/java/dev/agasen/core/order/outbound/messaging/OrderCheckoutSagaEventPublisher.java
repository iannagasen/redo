package dev.agasen.core.order.outbound.messaging;

import dev.agasen.api.events.OrderCheckoutSaga;
import dev.agasen.api.events.order.OrderCheckoutSagaEvent;
import dev.agasen.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCheckoutSagaEventPublisher implements EventPublisher< OrderCheckoutSagaEvent > {

   private final KafkaTemplate< Long, OrderCheckoutSaga > kafkaTemplate;

   @Value( "${system.topics.checkout.order.created}" )
   private String createdTopic;

   @Value( "${system.topics.checkout.order.confirmed}" )
   private String confirmedTopic;

   @Value( "${system.topics.checkout.order.cancelled}" )
   private String cancelledTopic;

   @Value( "${system.topics.checkout.order.failed}" )
   private String failedTopic;

   @Override
   public void publish( OrderCheckoutSagaEvent event ) {
      String topic = switch ( event ) {
         case OrderCheckoutSagaEvent.Created e -> createdTopic;
         case OrderCheckoutSagaEvent.Confirmed e -> confirmedTopic;
         case OrderCheckoutSagaEvent.Cancelled e -> cancelledTopic;
         case OrderCheckoutSagaEvent.Failed e -> failedTopic;
      };

      if ( topic == null ) {
         log.warn( "No topic configured for event type: {}", event.getClass().getSimpleName() );
         return;
      }

      log.info( "Publishing {} to topic '{}'", event.getClass().getSimpleName(), topic );
      kafkaTemplate.send( topic, event.orderId(), event );
   }
}
