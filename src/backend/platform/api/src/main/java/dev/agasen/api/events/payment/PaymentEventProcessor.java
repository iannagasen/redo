package dev.agasen.api.events.payment;

import dev.agasen.common.event.DomainEvent;
import dev.agasen.common.event.EventProcessor;

import java.util.Optional;

public interface PaymentEventProcessor< R extends DomainEvent > extends EventProcessor< PaymentEvent, R > {

   default Optional< R > process( PaymentEvent event ) {
      return switch ( event ) {
         case PaymentEvent.Processed e -> this.handle( e );
         case PaymentEvent.Refunded e -> this.handle( e );
         case PaymentEvent.Declined e -> this.handle( e );
      };
   }

   Optional< R > handle( PaymentEvent.Processed e );

   Optional< R > handle( PaymentEvent.Refunded e );

   Optional< R > handle( PaymentEvent.Declined e );

}
