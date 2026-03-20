# ShopBuddy

A full-stack e-commerce platform built with a microservices architecture, Spring Boot backend, and Angular frontend deployed on Kubernetes.

> Architecture diagrams, flow diagrams, and event flows: [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 24 | Primary language |
| Spring Boot 3 | Microservice framework |
| Spring Cloud Gateway (WebFlux) | API Gateway / reverse proxy |
| Spring Authorization Server | OAuth2 / JWT auth server |
| Spring Data JPA | Database ORM |
| Spring Data Redis | Caching |
| Spring Kafka | Event-driven messaging |
| Liquibase | Database migrations |
| MapStruct | DTO mapping |
| SpringDoc OpenAPI | Swagger UI |
| Gradle (multi-module) | Build tool |

### Frontend
| Technology | Purpose |
|---|---|
| Angular 20 | Storefront SPA |
| Tailwind CSS | Styling |
| Zoneless (Signals) | Change detection |

### Infrastructure
| Technology | Purpose |
|---|---|
| Kubernetes (Minikube) | Container orchestration |
| Skaffold | Local K8s dev workflow |
| Docker | Containerization |
| PostgreSQL 17 | Relational database |
| Redis | Caching + cart storage |
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
- **Java 24** (JDK)
- **Docker Desktop**
- **Node.js + Angular CLI** (`npm install -g @angular/cli`)

---

## Setup

### 1. Hosts File

Add these entries to `C:\Windows\System32\drivers\etc\hosts` (run as Administrator):

```
127.0.0.1 shopbuddy.com
127.0.0.1 redis.shopbuddy.com
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
# Storefront (port 4200)
cd src/frontend/storefront/storefront
ng serve

# K8s Dashboard (port 4300)
cd src/frontend/k8s-dashboard/k8s-dashboard
ng serve
```

---

## IntelliJ Run Configurations

Pre-configured in `.idea/runConfigurations/`:

| Configuration | Description |
|---|---|
| `Skaffold Dev` | Runs `skaffold dev --port-forward` |
| `Angular - storefront` | `ng serve` on port 4200 |
| `Angular - k8s-dashboard` | `ng serve` on port 4300 |
| `Remote Debug - auth-server` | Attaches to port 5005 |
| `Remote Debug - api-gateway` | Attaches to port 5006 |
| `Remote Debug - product-service` | Attaches to port 5007 |
| `Remote Debug - user-service` | Attaches to port 5008 |
| `Remote Debug - cart-service` | Attaches to port 5009 |
| `Remote Debug - order-service` | Attaches to port 5010 |
| `Remote Debug - payment-service` | Attaches to port 5011 |
| `Open All UIs` | Opens all UIs in Chrome |

### Remote Debugging

Forward the debug port before attaching IntelliJ:

```bash
# Example: debug order-service
kubectl port-forward deployment/order-deployment 5010:5010
```

Then run the corresponding `Remote Debug - *` config in IntelliJ.

---

## URLs

### Application UIs (via K8s Ingress)

| UI | URL |
|---|---|
| Storefront | http://shopbuddy.com/storefront |
| K8s Dashboard | http://shopbuddy.com/k8s-dashboard |
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
│   │   └── api/             # Shared DTOs and service interfaces
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
    ├── storefront/          # Main shopping app (port 4200)
    └── k8s-dashboard/       # K8s monitoring UI (port 4300)
k8s/                         # Kubernetes manifests
scripts/                     # Utility scripts
```

---

## Architecture

### Request Flow

```
Browser → NGINX Ingress → API Gateway → Microservice
                                      ↓
                              Auth Server (JWT validation)
```

### Event Flow (Kafka)

```
Order Service ──publishes──▶ payment-events ──consumes──▶ Payment Service
Payment Service ─publishes──▶ payment-result-events ──consumes──▶ Order Service
```

### Security

- **Auth flow**: PKCE Authorization Code (Angular → Auth Server)
- **Service auth**: JWT validated at API Gateway and each resource server
- **Scopes**: `product:read`, `product:write-create`, etc.
