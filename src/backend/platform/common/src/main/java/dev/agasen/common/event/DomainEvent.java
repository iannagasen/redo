package dev.agasen.common.event;

import java.time.Instant;
import java.util.UUID;

/// Base contract for every event that flows through the system.
/// All saga events, compensation events, and domain notifications implement this.
///
/// ## Fields
/// - `eventId` — unique ID for this specific event instance. Used for deduplication
///   at consumers; if the same event is delivered twice (Kafka at-least-once),
///   consumers check this to skip the duplicate.
/// - `occurredAt` — when the event *happened*, not when it was consumed or published.
public interface DomainEvent {

   /// Unique ID for this specific event instance. Used for deduplication at consumers.
   UUID eventId();

   /// Timestamp of when the event occurred.
   Instant occurredAt();

}
