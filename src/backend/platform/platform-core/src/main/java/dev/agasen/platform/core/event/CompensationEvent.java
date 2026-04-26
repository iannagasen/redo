package dev.agasen.platform.core.event;

/// Marker interface. Tags an event as a signal that a saga step failed
/// and upstream services must roll back their previously completed work.
///
/// No additional methods — the type itself is the signal.
///
/// ## Usage
/// Apply to failure events that require other services to compensate:
/// ```java
/// record Failed(...) implements PaymentEvent, CompensationEvent {}
/// ```
///
/// Enables generic compensation detection at runtime:
/// ```java
/// if (event instanceof CompensationEvent) { /* trigger rollback */ }
/// ```
///
/// Or enforce at compile time via type bounds:
/// ```java
/// EventProcessor<? extends CompensationEvent, OrderEvent.Cancelled>
/// ```
public interface CompensationEvent extends DomainEvent {
}
