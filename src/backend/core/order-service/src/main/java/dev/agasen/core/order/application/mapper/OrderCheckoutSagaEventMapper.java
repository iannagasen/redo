package dev.agasen.core.order.application.mapper;

import dev.agasen.api.events.order.OrderCheckoutSagaEvent;
import dev.agasen.core.order.domain.Order;

import java.time.Instant;
import java.util.UUID;

public class OrderCheckoutSagaEventMapper {

   public static OrderCheckoutSagaEvent.Confirmed toCheckoutConfirmedEvent( Order order ) {
      return OrderCheckoutSagaEvent.Confirmed.builder()
         .eventId( UUID.randomUUID() )
         .occurredAt( Instant.now() )
         .orderId( order.getId() )
         .userId( order.getUserId() )
         .build();
   }

   public static OrderCheckoutSagaEvent.Failed toCheckoutFailedEvent( Order order, String error ) {
      return OrderCheckoutSagaEvent.Failed.builder()
         .eventId( UUID.randomUUID() )
         .occurredAt( Instant.now() )
         .orderId( order.getId() )
         .errorMessage( error )
         .build();
   }
}
