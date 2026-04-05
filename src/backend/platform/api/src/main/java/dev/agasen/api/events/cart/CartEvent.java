package dev.agasen.api.events.cart;

import dev.agasen.api.events.OrderCheckoutSaga;
import dev.agasen.common.event.DomainEvent;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/// Sealed event hierarchy for the cart domain within the checkout saga.
///
/// ## Variants
/// - `Cleared` — published by cart-service after clearing the user's cart
///               on successful payment. Terminal event in the checkout saga.
public sealed interface CartEvent extends DomainEvent, OrderCheckoutSaga
   permits CartEvent.Cleared {


   /// Published by cart-service when the cart is cleared after a successful payment.
   /// Terminal event — no further saga steps follow this.
   @Builder
   record Cleared(
      UUID eventId,
      Instant occurredAt,
      Long orderId,
      String userId
   ) implements CartEvent {
   }

}
