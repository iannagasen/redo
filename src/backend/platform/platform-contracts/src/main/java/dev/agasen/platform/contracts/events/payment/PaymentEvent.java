package dev.agasen.platform.contracts.events.payment;

import dev.agasen.platform.contracts.events.OrderCheckoutSaga;
import dev.agasen.platform.core.event.DomainEvent;
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