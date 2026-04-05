# Running the whole app

using k8s and docker compose

```bash
make
```

# Preliminary setup

1. Add shopbuddy.com 127.0.0.1 in /etc/hosts

# Ports

| App                            | Local | Docker (Mapped) | Kubernetes      |
|--------------------------------|-------|-----------------|-----------------|
| StoreFront (Front End Channel) | 3000  | 8000            | -               |
| Product Service                | 8081  | 8080            | -               |
| API Gateway                    | 8000  | 8080            | 8000 (NodePort) |
| Auth Server                    | 8080  | 8080            | 8080            |
| Grafana                        | -     | -               | /grafana        |

## Credentials

| Service | Username | Password  |
|---------|----------|-----------|
| Grafana | admin    | shopbuddy |

k8s enpoints: using host (shopbuddy.com)
/product/actuator, /product/actuator/{health,info}
/product/public/hello
/swagger

http://shopbuddy.com/storefront

https://shopbuddy.com/product/swagger-ui/index.html#/product-controller/addProduct

temporarily disable the otlp logging by removing this in the environment variable

```text
-javaagent:src/backend/core/product-service/build/agent/opentelemetry-javaagent.jar
-Dotel.traces.exporter=logging
-Dotel.metrics.exporter=logging
-Dotel.logs.exporter=logging
```

# Improvement / issues

- Not using HTTPS on the domain (shopbuddy.com), we are using OAUTH2 so it should be https, there is a workaround
  in angular for this to use `npm install crypto-js` , uninstall once https is installed

# Future Plans

- observability
- messaging
    - kafka
- other inter service communication
    - no graphql ()
    - gRPC maybe
    - messaging
- documentation
- ci/cd
- k8s
    - helm / customize
- app
    - maybe some other technologies
- makefile flow, deployment like hot deploy only services with changes?

Dev Experience improvement

- use `telepresence` in conjunction with `skaffold`
    - Telepresence: preview urls, intercept

https://chatgpt.com/share/6959d854-75d0-8000-8f1d-00f746300770

# installations

```bash
choco install make
choco install minikube 
choco install kubectl
choco install -y skaffold
```

TODO: skaffold
create shortcut for starting up services - maybe use make


  1. Observability — Re-enable OpenTelemetry, deploy Jaeger/Zipkin + Prometheus/Grafana to K8s
  2. Inventory Service — Stock management to prevent race conditions on checkout
  3. Skaffold optimization — Integrate Gradle builds into Skaffold for faster dev loop (currently requires
  manual ./gradlew build first)
  4. CI/CD — GitHub Actions pipeline for build + test on push
  5. Frontend enhancements — Real-time order status updates via WebSockets, or NgRx state management
  6. HTTPS/TLS — cert-manager setup for shopbuddy.com
  7. LocalStack/MinIO

---

New Language

Inventory Service in Go
- Brand new microservice, so no migration cost
- Go's concurrency model (goroutines) is a great contrast to Java Virtual Threads
- The service is naturally simple: reserve/release stock, prevent overselling
- Integrates with your existing Kafka + K8s stack cleanly
- Forces you to think about how a non-Spring service consumes your OrderCheckoutSagaEvent

Cart Service in Kotlin
- Lowest friction — same JVM, same Gradle, Spring Boot supports Kotlin natively
- Kotlin coroutines + Flow are a more elegant story than reactive streams
- Lets you compare Kotlin DSL vs Java for Spring config (SecurityFilterChain, @Bean lambdas)

---
New Pattern

Transactional Outbox + Debezium CDC
- You currently have a dual-write risk in OrderCheckoutSagaInitiator: save to DB AND publish to Kafka in the
same method — if Kafka is down after the DB commit, the event is lost
- Debezium watches the Postgres WAL (write-ahead log) and streams changes to Kafka — atomicity is guaranteed
by the DB, not by your code
- This is a real production pattern used at Uber/LinkedIn
- New tech: Debezium connector running in your K8s cluster

Virtual Threads + Structured Concurrency (Java 24)
- You already use StructuredTaskScope in OrderSummaryAggregator — that's the foundation
- The interesting extension: replace @Async + thread pools with Virtual Threads everywhere, benchmark the
difference, and add a ShutdownOnFailure scope that cancels sibling calls if one external service times out
- It's "new" in the sense that most Spring Boot tutorials still teach the old thread pool model

---
New Protocol

gRPC for Order ↔ Inventory
- Protobuf contract in a .proto file replaces your api module DTOs for that boundary
- Strongly typed, generated client/server stubs, supports bidirectional streaming
- The checkout saga could call inventory via gRPC (sync reservation) while still publishing to Kafka for async
notification — good illustration of when to use each
- Interesting contrast: your REST APIs use OpenAPI, gRPC uses Protobuf — two schema-first approaches,
different tradeoffs

---
New Infra / Observability

OpenTelemetry distributed tracing end-to-end
- The OTel agent is already in the product-service Dockerfile but disabled
- Re-enable it, deploy Jaeger to K8s, and add manual span instrumentation inside the saga flow
- You'd see a single trace spanning: Angular → Gateway → Order → Kafka → Payment → Kafka → Order
(confirmation)
- Very visual payoff for relatively low code effort

---
My pick if I were you

gRPC for the Inventory Service (written in Go). You get:
- A new language (Go)
- A new protocol (gRPC/Protobuf)
- A new service that slots into the existing saga (INVENTORY participant in OrderCheckoutSagaInitiator)

Second choice: Debezium CDC outbox — it fixes an actual correctness bug in your current saga and the concept
is deeply useful in distributed systems work.