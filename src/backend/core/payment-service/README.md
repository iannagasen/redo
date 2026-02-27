# Payment Service

Handles payment processing for the ShopBuddy e-commerce platform. Sits between the frontend checkout
flow and external payment gateways, and notifies downstream services of results via Kafka.

---

## Checkout Flow

```
Frontend (cart)
  │
  ├─► POST /order/api/v1/orders          → order-service   (creates Order, status = PENDING)
  │
  ├─► navigate to /checkout?orderId=X
  │
  ├─► POST /payment/api/v1/payments      → payment-service (this service)
  │       │
  │       ├─ saves Payment (PENDING)
  │       ├─ calls PaymentGatewayClient.processPayment()
  │       ├─ updates Payment (CAPTURED | FAILED)
  │       └─ publishes PaymentEvent → Kafka topic "payment.result"
  │
  └─► polls GET /order/api/v1/orders/{id} until status ≠ PENDING

order-service (Kafka consumer)
  └─ @KafkaListener("payment.result")
       ├─ CAPTURED → order status = CONFIRMED
       └─ FAILED   → order status = PAYMENT_FAILED
```

---

## Design: Facade / Strategy Pattern

The core design goal is to isolate the business logic from any specific payment gateway.
The `PaymentGatewayClient` interface acts as a **facade** — the rest of the service only depends
on this contract, never on a concrete provider.

```
PaymentCommandService
      │
      └── PaymentGatewayClient  ◄── interface (the facade)
               │
               ├── MockPaymentGatewayClient   @Primary   (active by default)
               └── StripePaymentGatewayClient @Profile("stripe")  (future)
```

### Key Classes

| Class | Role |
|---|---|
| `PaymentGatewayClient` | Facade interface — swap gateways without touching business logic |
| `GatewayPaymentRequest` | Internal request model sent to the gateway |
| `GatewayPaymentResponse` | Internal response: `success`, `gatewayRef`, `failureReason` |
| `MockPaymentGatewayClient` | Simulates a real gateway. Card `4000000000000002` → declined, all others → success |
| `PaymentCommandService` | Orchestrates: save → call gateway → update status → publish event |
| `PaymentRetrievalService` | Read-only queries |
| `PaymentEventPublisher` | Thin wrapper around `KafkaTemplate` |
| `PaymentEventConsumer` | **In order-service** — consumes `payment.result` and updates order status |

### Adding a Real Gateway

1. Implement `PaymentGatewayClient`:
   ```java
   @Component
   @Profile("stripe")
   public class StripePaymentGatewayClient implements PaymentGatewayClient {
       @Override
       public GatewayPaymentResponse processPayment(GatewayPaymentRequest request) {
           // call Stripe SDK
       }
   }
   ```
2. Remove `@Primary` from `MockPaymentGatewayClient` (or keep it for non-stripe profiles).
3. Activate with `SPRING_PROFILES_ACTIVE=docker,stripe`.

No changes to `PaymentCommandService` required.

---

## Package Structure

```
dev.agasen.core.payment/
├── PaymentServiceApplication.java
├── PaymentRestService.java             REST controller
│
├── application/
│   ├── read/
│   │   └── PaymentRetrievalService.java    @Transactional(readOnly=true)
│   └── write/
│       └── PaymentCommandService.java      orchestrates gateway + Kafka
│
├── domain/
│   ├── Payment.java                    JPA entity (extends BaseEntity)
│   ├── PaymentRepository.java          Spring Data JPA
│   └── PaymentStatus.java              PENDING | CAPTURED | FAILED
│
├── gateway/
│   ├── PaymentGatewayClient.java       ← THE FACADE INTERFACE
│   ├── GatewayPaymentRequest.java      Lombok @Value @Builder
│   ├── GatewayPaymentResponse.java     Lombok @Value @Builder
│   └── mock/
│       └── MockPaymentGatewayClient.java   @Component @Primary
│
├── event/
│   └── PaymentEventPublisher.java      KafkaTemplate wrapper
│
└── config/
    └── SecurityConfig.java             OAuth2 JWT resource server (stateless)
```

---

## API Endpoints

All endpoints are secured (JWT required). Context path: `/payment`.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/payments` | Initiate a payment for an order |
| `GET` | `/api/v1/payments` | List current user's payments |
| `GET` | `/api/v1/payments/{id}` | Get a specific payment |

### POST /api/v1/payments — Request Body

```json
{
  "orderId": 123,
  "amount": 49.99,
  "currency": "USD",
  "cardNumber": "4242424242424242",
  "cardholderName": "Jane Doe",
  "expiryMonth": 12,
  "expiryYear": 2027,
  "cvv": "123"
}
```

### Response

```json
{
  "id": 1,
  "orderId": 123,
  "userId": "user-abc",
  "amount": 49.99,
  "currency": "USD",
  "status": "CAPTURED",
  "createdAt": "2026-02-24T10:00:00Z"
}
```

### Mock Card Numbers

| Card Number | Result |
|---|---|
| `4000000000000002` | FAILED (card declined) |
| Any other number | CAPTURED (success) |

---

## Database Schema

Database: `payment_service` (PostgreSQL)

```sql
CREATE TABLE payments (
    id              BIGINT PRIMARY KEY DEFAULT nextval('payments_seq'),
    order_id        BIGINT          NOT NULL,
    user_id         VARCHAR(255)    NOT NULL,
    amount          DECIMAL(19, 2)  NOT NULL,
    currency        VARCHAR(10),
    status          VARCHAR(50)     NOT NULL,   -- PENDING | CAPTURED | FAILED
    gateway_ref     VARCHAR(255),               -- reference from gateway on success
    failure_reason  VARCHAR(500),               -- reason string on failure
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);
```

Migrations are managed by Liquibase (`db/changelog/db.changelog-master.yaml`).

---

## Kafka Event

Published to topic `payment.result` after every payment attempt.

Defined in `platform:api` as a Java record:

```java
// dev.agasen.api.event.PaymentEvent
record PaymentEvent(
    Long orderId,
    Long paymentId,
    String userId,
    BigDecimal amount,
    String status,          // "CAPTURED" or "FAILED"
    String failureReason    // null on success
) {}
```

The `order-service` consumes this event via `@KafkaListener` and updates the order status accordingly.

---

## Configuration

### application.yml (local)

```yaml
server:
  port: 8085
  servlet:
    context-path: /payment

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_service
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false

env:
  base:
    url:
      internal:
        auth: http://localhost:8080
```

### Environment Variables (Docker / k8s)

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `postgres` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_USER` | `product_user` | DB username |
| `DB_PASSWORD` | `product_password` | DB password |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Kafka broker address |
| `INTERNAL_AUTH_SERVER_BASE_URL` | `http://auth-service:8080` | Auth server for JWKS |
| `SPRING_PROFILES_ACTIVE` | — | Set to `docker` in k8s |

---

## Security

Uses the same `SecurityConfig` pattern as all other resource servers in the platform:

- **`@Order(1)` public chain** — permits `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **`@Order(2)` secured chain** — stateless JWT validation via the auth server's JWKS endpoint
- No sessions, no form login, no CSRF

> **Critical:** Every service must define its own `SecurityConfig`. The common library's
> `ResourceServerSecurityConfiguration` autoconfiguration was removed. Without an explicit
> `SecurityConfig`, Spring Boot defaults to form-login with session cookies, causing redirect
> loops (`302 → /login → 302 → /login → ERR_TOO_MANY_REDIRECTS`).

---

## Running Locally

Prerequisites: PostgreSQL + Kafka running (see `docker-compose-infra.yaml`).

```bash
# Start infra
docker-compose -f docker-compose-infra.yaml up -d

# The init.sql must have already created the payment_service database, or create it manually:
psql -U product_user -d product_catalog -c "CREATE DATABASE payment_service;"

# Run the service
./gradlew :src:backend:core:payment-service:bootRun
# Accessible at http://localhost:8085/payment
```

---

## Running in Kubernetes

```bash
skaffold dev
# or
make rebuild
```

The k8s deployment init-container automatically creates the `payment_service` database if missing.
The ingress routes `shopbuddy.com/payment` → payment-service ClusterIP.
