# `dev.agasen.common.event` — Package Documentation

Foundation abstractions for the choreography-based saga system.
Lives in `platform:common` — shared across all services.

---

## Interface Hierarchy

```
DomainEvent
    └── CompensationEvent

Saga
    └── (extended per business transaction e.g. OrderSaga in platform:api)

EventProcessor<T extends DomainEvent, R extends DomainEvent>
EventPublisher<T extends DomainEvent>
```

---

## `DomainEvent`

**Type:** `interface`

The base contract for every event that flows through the system.
All saga events, compensation events, and domain notifications implement this.

| Method | Type | Description |
|---|---|---|
| `eventId()` | `UUID` | Unique ID for this specific event instance. Used for deduplication at consumers. |
| `occurredAt()` | `Instant` | Timestamp of when the event happened. |

**Usage:** Every event in the system implements this directly or via a sub-interface.

```
PaymentEvent.Captured implements PaymentEvent
PaymentEvent          extends DomainEvent
→ PaymentEvent.Captured is a DomainEvent
```

---

## `CompensationEvent`

**Type:** `interface extends DomainEvent`

Marker interface. Tags an event as a signal that a saga step failed and upstream services must roll back their work.

No additional methods — the type itself is the signal.

**When to use:** Apply to failure events that require other services to compensate.

```
PaymentEvent.Failed implements PaymentEvent, CompensationEvent
```

A generic handler can detect compensation events by type:
```java
if (event instanceof CompensationEvent) {
    // trigger rollback logic
}
```

Or enforce at compile time via type bounds:
```java
EventProcessor<CompensationEvent, OrderEvent.Cancelled>
```

---

## `Saga`

**Type:** `interface` (marker)

Root marker interface. Tags a type as belonging to a saga — a distributed business transaction spanning multiple services.

No methods. Used as a root for domain-specific saga markers.

**Extend this** to define a saga for a specific business flow:
```
OrderSaga extends Saga   ← in platform:api
```

Events tag themselves with the specific saga marker:
```
PaymentEvent extends DomainEvent, OrderSaga
OrderEvent   extends DomainEvent, OrderSaga
CartEvent    extends DomainEvent, OrderSaga
```

This enables type-safe publisher/processor bounds — only `OrderSaga`-tagged events can flow through an `OrderSaga` publisher.

---

## `EventProcessor<T, R>`

**Type:** `interface`

Represents one step in the saga pipeline. Consumes an incoming event of type `T`, executes a local transaction, and produces a result event of type `R`.

| Method | Signature | Description |
|---|---|---|
| `process` | `R process(T event)` | Handles the event, returns the next event in the chain. Blocking — structured concurrency or virtual threads handle async at the call site. |

**Key design decisions:**
- Returns `R` directly (not `Mono<R>`) — no reactive dependency, works with Java structured concurrency
- Both `T` and `R` are `DomainEvent` — type-safe pipeline steps
- `R` can be a sealed interface to represent branching outcomes (success or failure variant)

**Example chain:**
```
EventProcessor<OrderEvent.Created,  PaymentEvent>   // payment-service
EventProcessor<PaymentEvent.Captured, CartEvent>    // cart-service
```

---

## `EventPublisher<T>`

**Type:** `interface`

Transport abstraction for publishing events. Decouples the saga logic from the underlying messaging mechanism (Kafka or HTTP).

**Status:** Currently missing its `publish(T event)` method — needs to be added.

Planned implementations:
| Class | Description |
|---|---|
| `KafkaEventPublisher<T>` | Publishes via Kafka topic |
| `HttpEventPublisher<T>` | Publishes via direct HTTP call (fallback) |
| `RoutingEventPublisher<T>` | Delegates to Kafka or HTTP based on runtime `TransportMode` toggle |

---

## `Record`

**Type:** `class`

**Status:** Purpose unclear. Empty class that shadows `java.lang.Record` (the Java base class for record types). Candidate for removal.

---

## Saga Flow Using These Abstractions

```
Order Service                    Payment Service               Cart Service
─────────────                    ───────────────               ────────────
creates order
publishes OrderEvent.Created ──► EventProcessor handles it
                                 processes payment
                                 publishes PaymentEvent ──────► EventProcessor handles it
                                   .Captured                   clears cart
                                   .Failed ─────────────────►
                                                               Order Service handles
                                                               compensation — cancels order
```
