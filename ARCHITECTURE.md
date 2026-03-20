# ShopBuddy — Architecture & Flow Diagrams

---

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph Client["Client Layer"]
        Browser["Browser"]
        Storefront["Angular Storefront\nlocalhost:4200"]
        Dashboard["K8s Dashboard\nlocalhost:4300"]
    end

    subgraph K8s["Kubernetes Cluster (Minikube)"]
        Ingress["NGINX Ingress\nshopbuddy.com / redis.shopbuddy.com"]

        subgraph Infra["Infrastructure Services"]
            Gateway["API Gateway\n:8080"]
            Auth["Auth Server\n:8080"]
        end

        subgraph Core["Core Services"]
            Product["Product Service\n:8080"]
            User["User Service\n:8080"]
            Cart["Cart Service\n:8080"]
            Order["Order Service\n:8080"]
            Payment["Payment Service\n:8080"]
        end

        subgraph Data["Data Layer"]
            PG[("PostgreSQL\n:5432")]
            Redis[("Redis\n:6379")]
            Kafka[("Kafka\n:9092")]
        end

        subgraph Tooling["Dev Tooling"]
            KafkaUI["Kafka UI"]
            RedisInsight["Redis Insight"]
            K8sAdmin["K8s Admin"]
        end
    end

    Browser --> Ingress
    Storefront -->|proxy| Ingress
    Ingress --> Gateway
    Ingress --> K8sAdmin
    Ingress --> KafkaUI
    Ingress -->|redis.shopbuddy.com| RedisInsight

    Gateway --> Auth
    Gateway --> Product
    Gateway --> User
    Gateway --> Cart
    Gateway --> Order
    Gateway --> Payment

    Auth --> PG
    Product --> PG
    Product --> Redis
    User --> PG
    Cart --> Redis
    Order --> PG
    Order --> Kafka
    Payment --> PG
    Payment --> Kafka

    KafkaUI --> Kafka
    RedisInsight --> Redis
```

---

## 2. Kubernetes Deployment Layout

```mermaid
graph TB
    subgraph Cluster["Minikube Cluster"]
        subgraph ingress_ns["ingress-nginx"]
            NGINX["ingress-nginx-controller"]
        end

        subgraph default_ns["default namespace"]
            subgraph Deployments["Deployments"]
                GW["gateway-deployment"]
                AUTH["auth-deployment"]
                PROD["product-deployment"]
                USER["user-deployment"]
                CART["cart-deployment"]
                ORD["order-deployment"]
                PAY["payment-deployment"]
                KA["k8s-admin-deployment"]
                KUI["kafka-ui"]
                RI["redis-insight-deployment"]
            end

            subgraph StatefulSets["StatefulSets"]
                PG[("postgres-0")]
                RD[("redis-0")]
                KF[("kafka-0")]
            end
        end
    end

    Internet["Internet / minikube tunnel"] --> NGINX
    NGINX --> GW & KA & KUI & RI
    GW --> AUTH & PROD & USER & CART & ORD & PAY

    PROD & USER & AUTH & ORD & PAY --> PG
    PROD & CART --> RD
    ORD & PAY --> KF
    KUI --> KF
    RI --> RD
```

---

## 3. Service Internal Architecture

Each core service follows the same layered pattern:

```mermaid
graph TB
    subgraph Service["Core Service (e.g. product-service)"]
        subgraph REST["Infrastructure — REST"]
            PubCtrl["Public Controller\n/public/** (no auth)"]
            SecCtrl["Secured Controller\n(JWT required)"]
        end

        subgraph App["Application Layer"]
            ReadSvc["Read Service\n@Transactional(readOnly=true)"]
            WriteSvc["Write Service\n(Command pattern)"]
            Mapper["MapStruct Mapper"]
        end

        subgraph Domain["Domain Layer"]
            Entity["JPA Entity\nextends BaseEntity"]
            Repo["Spring Data Repository"]
        end

        subgraph Infra2["Infrastructure — Cross-cutting"]
            Security["SecurityFilterChain\n(2 chains: public + secured)"]
            Cache["RedisCachingTemplate\ngetCachedOrCompute()"]
        end
    end

    PubCtrl & SecCtrl --> ReadSvc & WriteSvc
    ReadSvc & WriteSvc --> Mapper
    ReadSvc & WriteSvc --> Repo
    Repo --> Entity
    Entity --> PG[("PostgreSQL")]
    ReadSvc --> Cache
    Cache --> Redis[("Redis")]
    Security --> SecCtrl
```

---

## 4. OAuth2 PKCE Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant Angular
    participant AuthServer as Auth Server<br/>/auth
    participant Gateway as API Gateway
    participant Service as Resource Service

    User->>Angular: Click Login
    Angular->>Angular: Generate code_verifier + code_challenge (PKCE)
    Angular->>AuthServer: GET /auth/oauth2/authorize<br/>(client_id, code_challenge, redirect_uri)
    AuthServer->>User: Render Login Page
    User->>AuthServer: Submit credentials
    AuthServer->>Angular: Redirect with authorization_code
    Angular->>AuthServer: POST /auth/oauth2/token<br/>(code + code_verifier)
    AuthServer->>Angular: Access Token (JWT)

    User->>Angular: Perform action (e.g. view products)
    Angular->>Gateway: GET /product/api/products<br/>Authorization: Bearer <JWT>
    Gateway->>AuthServer: Validate JWT via JWKS endpoint
    AuthServer->>Gateway: Token valid
    Gateway->>Service: Forward request
    Service->>Gateway: Response
    Gateway->>Angular: Response
    Angular->>User: Display data
```

---

## 5. Add to Cart Flow

```mermaid
sequenceDiagram
    actor User
    participant Angular
    participant Gateway as API Gateway
    participant Cart as Cart Service
    participant Redis

    User->>Angular: Click "Add to Cart"
    Angular->>Gateway: POST /cart/items<br/>Bearer Token + item payload
    Gateway->>Gateway: Validate JWT
    Gateway->>Cart: POST /cart/items

    Cart->>Redis: GET cart:{userId}
    alt Cart exists
        Redis-->>Cart: Return existing cart
    else No cart found
        Cart->>Redis: SET cart:{userId} (new cart)
    end

    Cart->>Redis: Update cart items
    Redis-->>Cart: OK
    Cart-->>Gateway: CartDetails response
    Gateway-->>Angular: CartDetails response
    Angular-->>User: Update cart icon + drawer
```

---

## 6. Checkout & Payment Flow

```mermaid
sequenceDiagram
    actor User
    participant Angular
    participant Gateway as API Gateway
    participant Order as Order Service
    participant Payment as Payment Service
    participant Kafka
    participant PG as PostgreSQL
    participant MockGW as Mock Payment Gateway

    User->>Angular: Click "Place Order"
    Angular->>Gateway: POST /order<br/>Bearer Token + order payload
    Gateway->>Order: Create Order
    Order->>PG: INSERT order (status=PENDING)
    Order->>Kafka: Publish → payment-events<br/>{orderId, userId, amount}
    Order-->>Angular: 201 Created (PENDING)

    Note over Kafka,Payment: Async event processing

    Kafka->>Payment: Consume payment-events
    Payment->>PG: INSERT payment (status=PROCESSING)
    Payment->>MockGW: Process payment request
    MockGW-->>Payment: Payment result

    alt Payment Successful
        Payment->>PG: UPDATE payment (status=COMPLETED)
        Payment->>Kafka: Publish → payment-result-events (SUCCESS)
        Kafka->>Order: Consume payment-result-events
        Order->>PG: UPDATE order (status=COMPLETED)
    else Payment Failed
        Payment->>PG: UPDATE payment (status=FAILED)
        Payment->>Kafka: Publish → payment-result-events (FAILED)
        Kafka->>Order: Consume payment-result-events
        Order->>PG: UPDATE order (status=FAILED)
    end

    User->>Angular: Poll order status
    Angular->>Gateway: GET /order/{orderId}
    Gateway->>Order: Get order
    Order->>PG: SELECT order
    Order-->>Angular: OrderDetails (COMPLETED or FAILED)
    Angular-->>User: Show order confirmation / error
```

---

## 7. Kafka Event Flow

```mermaid
flowchart LR
    subgraph OrderSvc["Order Service"]
        OC["OrderCommandService"]
        OE["PaymentEventConsumer"]
    end

    subgraph PaymentSvc["Payment Service"]
        PC["PaymentCommandService"]
        PE["PaymentEventPublisher"]
    end

    subgraph Topics["Kafka Topics"]
        T1[["payment-events"]]
        T2[["payment-result-events"]]
    end

    OC -->|publish| T1
    T1 -->|consume| PC
    PC --> PE
    PE -->|publish| T2
    T2 -->|consume| OE
    OE -->|update order status| OC
```

---

## 8. Request Routing (Ingress → Gateway → Services)

```mermaid
flowchart TD
    Client["Browser / Angular"] -->|HTTP| Ingress["NGINX Ingress\nshopbuddy.com"]

    Ingress -->|/product/**| GW
    Ingress -->|/cart/**| GW
    Ingress -->|/order/**| GW
    Ingress -->|/payment/**| GW
    Ingress -->|/auth/**| GW
    Ingress -->|/kafka-ui/**| KafkaUI["Kafka UI"]
    Ingress -->|/k8s-admin/**| K8sAdmin["K8s Admin"]
    Ingress -->|/storefront/**| Storefront["Storefront (K8s)"]
    Ingress -->|redis.shopbuddy.com| RedisInsight["Redis Insight"]

    GW["API Gateway"] -->|validate JWT| Auth["Auth Server"]
    GW -->|/product/**| Product["Product Service"]
    GW -->|/cart/**| Cart["Cart Service"]
    GW -->|/order/**| Order["Order Service"]
    GW -->|/payment/**| Payment["Payment Service"]
    GW -->|/user/**| User["User Service"]
```

---

## 9. Database Layout (Conceptual)

```mermaid
erDiagram
    PRODUCT {
        bigint id PK
        string name
        string description
        decimal price
        int stock
        timestamp created_date
        timestamp last_modified_date
    }

    USER {
        bigint id PK
        string username
        string email
        string role
        timestamp created_date
    }

    ORDER {
        bigint id PK
        bigint user_id FK
        string status
        decimal total_amount
        timestamp created_date
    }

    ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        bigint product_id
        int quantity
        decimal price
    }

    PAYMENT {
        bigint id PK
        bigint order_id FK
        bigint user_id
        decimal amount
        string status
        timestamp created_date
    }

    ORDER ||--o{ ORDER_ITEM : contains
    ORDER ||--|| PAYMENT : has
```

---

## 10. Security Filter Chains

Each service has two `SecurityFilterChain` beans:

```mermaid
flowchart TD
    Request["Incoming Request"]
    Request --> Chain1

    subgraph Chain1["@Order(1) — Public Chain"]
        P1{{"Path matches\n/actuator/**\n/swagger-ui/**\n/v3/api-docs/**\n/public/**"}}
        P1 -->|yes| Permit["Permit All"]
        P1 -->|no| Skip1["Skip to next chain"]
    end

    Skip1 --> Chain2

    subgraph Chain2["@Order(2) — Secured Chain"]
        P2{{"Has valid JWT?"}}
        P2 -->|yes| Scope{{"Has required scope?\ne.g. product:read"}}
        P2 -->|no| Reject["401 Unauthorized"]
        Scope -->|yes| Allow["Allow"]
        Scope -->|no| Forbidden["403 Forbidden"]
    end
```
