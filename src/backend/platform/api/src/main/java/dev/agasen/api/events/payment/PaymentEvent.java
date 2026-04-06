package dev.agasen.api.events.payment;

import dev.agasen.api.events.OrderCheckoutSaga;
import dev.agasen.common.event.DomainEvent;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public sealed interface PaymentEvent extends DomainEvent, OrderCheckoutSaga permits PaymentEvent.Declined, PaymentEvent.Refunded, PaymentEvent.Processed {
   
   @Builder
   public record Processed(
      UUID eventId,
      Instant occurredAt,
      Long orderId
   ) implements PaymentEvent {
   }

   @Builder
   record Declined(
      UUID eventId,
      Instant occurredAt,
      Long orderId,
      String message
   ) implements PaymentEvent {
   }

   @Builder
   record Refunded(
      UUID eventId,
      Instant occurredAt,
      Long orderId
   ) implements PaymentEvent {
   }


}