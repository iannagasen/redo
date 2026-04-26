# ShopBuddy

A full-stack e-commerce platform built as a microservices reference project. Spring Boot backend, Angular storefront, OAuth2 PKCE authentication, async payment processing via Kafka, and Kubernetes deployment on Minikube.

> Architecture diagrams, flow diagrams, and event flows: [ARCHITECTURE.md](./ARCHITECTURE.md)


---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 26 | Primary language |
| Spring Boot 4.0.4 | Microservice framework |
| Spring Cloud 2025.1.1 | Gateway, config |
| Spring Authorization Server | OAuth2 / JWT auth server |
| Spring Data JPA | Database ORM |
| Spring Data Redis | Caching + cart storage |
| Spring Kafka | Event-driven messaging |
| Liquibase | Database migrations |
| MapStruct | DTO mapping |
| SpringDoc OpenAPI | Swagger UI |
| Gradle (multi-module) | Build tool |

### Frontend
| Technology | Purpose |
|---|---|
| Angular 19+ | Storefront SPA |
| Tailwind CSS | Styling |
| Zoneless (Signals) | Change detection |

### Infrastructure
| Technology | Purpose |
|---|---|
| Kubernetes (Minikube) | Container orchestration |
| Skaffold | Local K8s dev workflow |
| Docker | Containerization |
| PostgreSQL 17 | Relational database |
| Redis 6 | Caching + cart storage |
| Apache Kafka | Event streaming |
| NGINX Ingress | Ingress controller |

---

## Services

| Service | Description | Local Port | Debug Port |
|---|---|---|---|
| `api-gateway` | Spring Cloud Gateway — routes all traffic | 8000 | 5006 |
| `auth-server` | OAuth2 Authorization Server (JWT/PKCE) | 8080 | 5005 |
| `product-service` | Product catalog | 8081 | 5007 |
| `user-service` | User management | 8080 | 5008 |
| `cart-service` | Shopping cart (Redis-backed) | 8082 | 5009 |
| `order-service` | Order management (Kafka consumer) | 8083 | 5010 |
| `payment-service` | Payment processing (Kafka producer) | 8085 | 5011 |
| `k8s-admin` | Kubernetes API wrapper | — | — |

---

## Prerequisites

Install via [Chocolatey](https://chocolatey.org/) (Windows):

```powershell
choco install minikube
choco install kubectl
choco install skaffold
choco install make
```

Also required:
- **Java 26** (JDK)
- **Docker Desktop**
- **Node.js + Angular CLI** (`npm install -g @angular/cli`)

---

## Setup

### 1. Hosts File

Add these entries to `C:\Windows\System32\drivers\etc\hosts` (run as Administrator):

```
127.0.0.1 shopbuddy.com
127.0.0.1 redis.shopbuddy.com
127.0.0.1 localstack
```

### 2. Enable Minikube Ingress Addon

```bash
minikube addons enable ingress
```

---

## How to Start

### Backend (Kubernetes via Skaffold)

Open **3 separate terminals**:

```bash
# Terminal 1 — start Minikube
minikube start

# Terminal 2 — tunnel ingress traffic (requires admin/elevated privileges)
minikube tunnel

# Terminal 3 — build images + deploy to K8s + watch for changes
skaffold dev
```

> After deploy, delete the admission webhook to prevent ingress errors:
> ```bash
> kubectl delete validatingwebhookconfiguration ingress-nginx-admission
> ```

### Frontend

```bash
cd src/frontend/storefront/storefront
ng serve
# open http://localhost:4200/storefront/
```

---

## IntelliJ Run Configurations

Pre-configured in `.idea/runConfigurations/`:

| Configuration | Description |
|---|---|
| `Skaffold Dev` | Runs `skaffold dev --port-forward` |
| `Angular - storefront` | `ng serve` on port 4200 |
| `Remote Debug - auth-server` | Attaches to port 5005 |
| `Remote Debug - api-gateway` | Attaches to port 5006 |
| `Remote Debug - product-service` | Attaches to port 5007 |
| `Remote Debug - user-service` | Attaches to port 5008 |
| `Remote Debug - cart-service` | Attaches to port 5009 |
| `Remote Debug - order-service` | Attaches to port 5010 |
| `Remote Debug - payment-service` | Attaches to port 5011 |

### Remote Debugging

Forward the debug port before attaching IntelliJ:

```bash
# Example: debug order-service
kubectl port-forward deployment/order-deployment 5010:5010
```

---

## URLs

### Application UIs (via K8s Ingress)

| UI | URL |
|---|---|
| Storefront | http://shopbuddy.com/storefront |
| Kafka UI | http://shopbuddy.com/kafka-ui |
| Redis Insight | http://redis.shopbuddy.com |

### Swagger UIs

| Service | URL |
|---|---|
| Product | http://shopbuddy.com/product/swagger-ui.html |
| Cart | http://shopbuddy.com/cart/swagger-ui.html |
| Order | http://shopbuddy.com/order/swagger-ui.html |
| Payment | http://shopbuddy.com/payment/swagger-ui.html |

### Local Dev (without K8s)

| Service | URL |
|---|---|
| Product Swagger | http://localhost:8081/product/swagger-ui.html |
| Cart Swagger | http://localhost:8082/cart/swagger-ui.html |
| Order Swagger | http://localhost:8083/order/swagger-ui.html |
| Payment Swagger | http://localhost:8085/payment/swagger-ui.html |
| Auth Server | http://localhost:8080/auth |

---

## Build Commands

```bash
# Build all backend services (skip tests)
./gradlew clean build -x test

# Run all tests
./gradlew test

# Run tests for a specific service
./gradlew :src:backend:core:product-service:test
./gradlew :src:backend:core:user-service:test
./gradlew :src:backend:infra:auth-server:test

# Full pipeline: build + docker + k8s deploy
make

# Rebuild Docker images only (after code changes)
make rebuild

# Exclude a service from the build
make EXCLUDE=storefront rebuild

# Deploy to K8s only (images already built)
make k8s-up
```

---

## Project Structure

```
src/
├── backend/
│   ├── platform/
│   │   ├── common/          # Shared utilities: CQRS, caching, validation
│   │   └── api/             # Shared DTOs, service interfaces, Kafka events
│   ├── core/
│   │   ├── product-service/
│   │   ├── user-service/
│   │   ├── cart-service/
│   │   ├── order-service/
│   │   └── payment-service/
│   └── infra/
│       ├── api-gateway/
│       ├── auth-server/
│       └── k8s-admin/
└── frontend/
    └── storefront/          # Main shopping app (port 4200)
k8s/                         # Kubernetes manifests
```

---

## Architecture

### Checkout Flow

```
1. POST /order/api/v1/orders      → order-service creates Order (PENDING)
2. Navigate to /checkout?orderId=X
3. POST /payment/api/v1/payments  → payment-service saves Payment, calls gateway,
                                    publishes PaymentEvent → Kafka "payment.result"
4. Kafka consumer (order-service) → updates Order to CONFIRMED | PAYMENT_FAILED
5. Frontend polls GET /orders/{id} → detects settled status, shows result page
```

### Event Flow (Kafka)

```
payment-service  ──publishes──▶  payment.result  ──consumes──▶  order-service
```

### Security

- **Auth flow**: PKCE Authorization Code (Angular → Auth Server)
- **Service auth**: JWT validated at each resource server via JWKS endpoint
- **Scopes**: `product:read`, `product:write-create`, `cart:read`, `cart:write`, etc.

---

## Mock Credentials

| Resource | Value |
|---|---|
| Login | `admin` / `pass` |
| Payment success | Any card number |
| Payment decline | `4000000000000002` |

---

## Roadmap

- [ ] Observability — re-enable OpenTelemetry agent, activate Jaeger + Prometheus/Grafana
- [ ] Inventory Service — stock management to prevent overselling race conditions
- [ ] HTTPS/TLS — cert-manager with self-signed certs for `shopbuddy.com`
- [ ] CI/CD — GitHub Actions pipeline (build + test on push)
- [ ] Kustomize — replace raw k8s YAML with Kustomize overlays for dev/prod environments
- [ ] Skaffold hot reload — integrate Gradle builds into Skaffold for faster dev loop
- [ ] Real-time order updates — WebSocket push for status changes
- [ ] gRPC — explore for high-throughput inter-service calls (e.g. Order ↔ Inventory)

- [ ] Should have configuration to not start the observability/monitoring/other infra image when skaffold dev
- [ ] Learn about live property change with config map, say you want to set some properties live, but dont start the server

---

## Features & Capabilities

### Storefront (Angular)
- Product catalog with paginated browsing and brand filtering
- Product detail page with add-to-cart
- Shopping cart — add, update quantity, remove items; persists across devices via Redis
- Checkout flow: cart → place order → payment form → live status polling → confirmation
- Mock payment: any card succeeds; `4000000000000002` simulates a decline
- Order history list with status badges (PENDING, CONFIRMED, PAYMENT_FAILED, etc.)
- Order summary detail page — itemized breakdown with product descriptions + payment receipt
- OAuth2 PKCE login / logout via Angular-managed auth flow
- Route guards protecting all authenticated pages
- Zoneless change detection using Angular signals throughout

### Product Service
- Paginated product listing and single-product lookup
- Brand autocomplete / search
- Admin endpoint to create products (requires `SCOPE_product:write-create`)
- Redis caching: individual products (`product:{id}`) and pages (`page:p-{page}:s-{size}`), 10-minute TTL
- OpenAPI / Swagger UI at `/product/swagger-ui.html`

### Cart Service
- Redis-backed cart keyed by user ID (cross-device persistence)
- Add items, update quantity, remove items
- Cart total and item count computed server-side

### Order Service
- Create orders from a list of cart items
- Order status lifecycle: `PENDING` → `CONFIRMED` | `PAYMENT_FAILED`
- List all orders for the authenticated user
- Order summary endpoint: enriched items (name, brand, description, line total) + linked payment details
- Kafka consumer on topic `payment.result` — updates order status when payment settles

### Payment Service
- Initiates payment for an order (amount, currency, card details)
- Facade pattern over `PaymentGatewayClient` — swap payment providers without touching business logic
- Active mock gateway: card `4000000000000002` → declined, any other number → success
- Publishes `PaymentEvent` to Kafka topic `payment.result` after every attempt
- Stripe provider stub ready (`@Profile("stripe")`)

### Auth Server
- Spring Authorization Server with RSA-signed JWTs
- OAuth2 Authorization Code + PKCE flow (Angular storefront)
- OAuth2 Client Credentials flow (service-to-service)
- Clients: `angular-client` (PKCE), `internal-client` (client credentials)
- Default credentials: `admin` / `pass`

### API Gateway
- Single entry point routing all traffic under `shopbuddy.com`
- Spring Cloud Gateway (WebFlux)

### Security (all resource servers)
- Stateless JWT validation via auth server's JWKS endpoint
- Two `SecurityFilterChain` beans per service: public chain (actuator, swagger) and secured chain
- Scope-based method security (`@PreAuthorize("hasAuthority('SCOPE_...')")`)
- Security - gRPC and Kafka
- GRPC load balancing
    - hard to achieve since it uses HTTP2 long live connection. Explain how to LB gRPC

### Infrastructure
- Database migrations managed by Liquibase
- PostgreSQL per service (product, user, order, payment)
- Redis for cart and product cache
- Kafka for async payment result events
- Kubernetes manifests for all services, infra, and ingress
- Observability manifests included (Prometheus, Grafana, Jaeger — infrastructure in place)


GRPC
-   5. No observability on the gRPC calls

    You have OpenTelemetry set up for HTTP calls already. gRPC calls are currently
    invisible to your tracing stack — no spans, no metrics.

    Add grpc-java's OpenTelemetry interceptor:
    io.opentelemetry.instrumentation:opentelemetry-grpc-1.6

    This auto-creates spans for every gRPC call and propagates trace context
    between auth-server and user-service — so a login trace shows the gRPC call as
    a child span.

-   1. Security — mTLS (most important)

    Right now the gRPC channel uses .usePlaintext() — no encryption, no
    authentication. Any pod in the cluster can call user-service:9090 and get
    password hashes back.

    The fix is mutual TLS (mTLS):
    - Both sides present certificates
    - auth-server proves it's auth-server, user-service proves it's user-service
    - Traffic is encrypted

    In Kubernetes you already have cert-manager. You'd:
    1. Create a Certificate resource for user-service gRPC
    2. Mount the cert as a Kubernetes secret into both pods
    3. Replace .usePlaintext() with .useTransportSecurity() + load the certs

    This is the single most important improvement — password hashes travelling in
    plaintext over the network is a real vulnerability even inside a cluster.