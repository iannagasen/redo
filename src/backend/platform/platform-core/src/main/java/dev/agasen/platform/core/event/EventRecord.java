package dev.agasen.platform.core.event;

/// Wraps an event with a routing key for transport.
/// Used by publishers to associate a message with its partition key
/// (typically the saga/order ID) without coupling the event itself to transport concerns.
///
/// ## Example
/// ```java
/// new EventRecord<>(orderId.toString(), paymentCapturedEvent)
/// ```
public record EventRecord< T >(
   String key,
   T message
) {
}
