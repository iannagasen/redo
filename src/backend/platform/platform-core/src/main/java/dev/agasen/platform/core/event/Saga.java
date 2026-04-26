package dev.agasen.platform.core.event;

/// Root marker interface for saga participation.
/// Tags a type as belonging to a distributed business transaction
/// that spans multiple services.
///
/// ## Usage
/// Do not implement this directly on events. Instead, extend it to define
/// a marker for a specific business flow in `platform:api`:
/// ```java
/// public interface OrderSaga extends Saga {}
/// ```
///
/// Then tag your sealed event interfaces with the specific saga marker:
/// ```java
/// public sealed interface PaymentEvent extends DomainEvent, OrderSaga { ... }
/// public sealed interface OrderEvent   extends DomainEvent, OrderSaga { ... }
/// public sealed interface CartEvent    extends DomainEvent, OrderSaga { ... }
/// ```
///
/// This enables type-safe publisher and processor bounds — only events
/// tagged with `OrderSaga` can flow through the order saga's pipeline.
public interface Saga {
}
