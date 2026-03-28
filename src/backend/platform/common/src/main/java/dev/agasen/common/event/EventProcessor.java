package dev.agasen.common.event;

/// Represents one step in the saga pipeline.
/// Consumes an incoming event of type `T`, executes a local transaction,
/// and produces a result event of type `R`.
///
/// ## Design decisions
/// - Returns `R` directly (not `Mono<R>`) — no reactive dependency.
///   Async coordination is handled at the call site via structured concurrency
///   or virtual threads.
/// - Both `T` and `R` are `DomainEvent` — fully type-safe pipeline steps.
/// - `R` can be a sealed interface to represent branching outcomes:
///   ```java
///   EventProcessor<OrderEvent.Created, PaymentEvent>
///   // PaymentEvent is sealed: Captured | Failed
///   ```
///
/// ## Example chain
/// ```
/// OrderEvent.Created  →  PaymentProcessor  →  PaymentEvent
/// PaymentEvent        →  CartProcessor     →  CartEvent
/// ```
public interface EventProcessor< T extends DomainEvent, R extends DomainEvent > {

   /// Handles the incoming event, executes a local transaction,
   /// and returns the next event in the saga chain.
   R process( T event );

}
