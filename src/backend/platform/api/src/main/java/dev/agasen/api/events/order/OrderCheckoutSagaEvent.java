package dev.agasen.api.events.order;

import dev.agasen.api.core.order.write.OrderItemRequest;
import dev.agasen.api.events.OrderCheckoutSaga;
import dev.agasen.common.event.CompensationEvent;
import dev.agasen.common.event.DomainEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/// Sealed event hierarchy for the order domain within the checkout saga.
///
/// ## Variants
/// - `Created`   — saga entry point, published by order-service after saving the order.
///                 Triggers payment-service to initiate payment.
/// - `Confirmed` — published when payment succeeds and the order is confirmed.
/// - `Cancelled` — published when the order is cancelled due to payment failure.
///                 Implements `CompensationEvent` — signals upstream compensation is complete.
public sealed interface OrderCheckoutSagaEvent extends DomainEvent, OrderCheckoutSaga permits
   OrderCheckoutSagaEvent.Created, OrderCheckoutSagaEvent.Confirmed, OrderCheckoutSagaEvent.Cancelled,
   OrderCheckoutSagaEvent.Failed {

   /// Published by order-service immediately after saving a new order.
   /// This event starts the checkout saga — payment-service listens to this.
   ///
   /// Carries payment details so payment-service can initiate the charge.
   @Builder
   record Created(
      UUID eventId,
      Instant occurredAt,
      Long orderId,
      String userId,
      List< OrderItemRequest > items,
      BigDecimal total,
      String cardNumber,
      String cardholderName,
      Integer expiryMonth,
      Integer expiryYear,
      String cvv
   ) implements OrderCheckoutSagaEvent {}

   /// Published by order-service when payment is captured and order is confirmed.
   @Builder
   record Confirmed(
      UUID eventId,
      Instant occurredAt,
      Long orderId,
      String userId
   ) implements OrderCheckoutSagaEvent {}

   /// Published by order-service when the order is cancelled after a payment failure.
   /// Implements `CompensationEvent` — marks the end of the compensation path.
   @Builder
   record Cancelled(
      UUID eventId,
      Instant occurredAt,
      Long orderId,
      String userId,
      String reason
   ) implements OrderCheckoutSagaEvent, CompensationEvent {}

   @Builder
   record Failed(
      UUID eventId,
      Instant occurredAt,
      Long orderId,
      String errorMessage
   ) implements OrderCheckoutSagaEvent {}

}
