package dev.agasen.platform.core.event;

/// Transport abstraction for publishing saga events.
/// Decouples saga logic from the underlying messaging mechanism.
///
/// ## Planned implementations
/// - `KafkaEventPublisher<T>` — publishes to a Kafka topic (default)
/// - `HttpEventPublisher<T>` — publishes via direct HTTP call (fallback)
/// - `RoutingEventPublisher<T>` — delegates to Kafka or HTTP based on
///   the runtime `TransportMode` toggle
public interface EventPublisher< T extends DomainEvent > {

   /// Publishes the event to the underlying transport (Kafka or HTTP).
   void publish( T event );

}
