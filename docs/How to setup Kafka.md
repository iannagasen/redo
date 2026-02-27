# How to Setup Kafka

This guide covers running Kafka in KRaft mode (no Zookeeper), wiring it into Spring Boot services
as a producer and consumer, and monitoring it with Kafka UI — both locally via Docker Compose and
in Kubernetes via Skaffold.

---

## 1. Kafka Fundamentals

### What is Kafka?

Apache Kafka is a distributed event streaming platform. In this project it acts as the async
message bus between the payment-service (producer) and the order-service (consumer).

```
payment-service  ──publish──►  [topic: payment.result]  ──consume──►  order-service
```

### KRaft Mode (no Zookeeper)

Since Kafka 3.3, Kafka can run without Zookeeper using its built-in **KRaft** consensus protocol.
A single node acts as both `broker` and `controller`. This simplifies local dev significantly —
one container, no dependency chain.

### Key Concepts

| Term | Description |
|---|---|
| **Topic** | Named channel for messages (e.g. `payment.result`) |
| **Producer** | Service that writes messages to a topic |
| **Consumer** | Service that reads messages from a topic |
| **Consumer Group** | Group of consumers sharing the read load. Each message is delivered to one member of the group |
| **Offset** | Position of a consumer within a topic partition. Kafka tracks this per group |
| **Bootstrap Servers** | The broker address(es) a client connects to initially |

---

## 2. Docker Compose (Local Development)

Add to `docker-compose-infra.yaml`:

```yaml
kafka:
  image: apache/kafka:3.9.0
  container_name: kafka
  ports:
    - "9092:9092"
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

kafka-ui:
  image: provectuslabs/kafka-ui:latest
  container_name: kafka-ui
  ports:
    - "9090:8080"
  environment:
    KAFKA_CLUSTERS_0_NAME: local
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
  depends_on:
    - kafka
```

### Environment Variable Reference

| Variable | Value | Why |
|---|---|---|
| `KAFKA_NODE_ID` | `1` | Unique ID for this broker node |
| `KAFKA_PROCESS_ROLES` | `broker,controller` | KRaft: this node handles both roles |
| `KAFKA_LISTENERS` | `PLAINTEXT://:9092,CONTROLLER://:9093` | Internal listener addresses |
| `KAFKA_ADVERTISED_LISTENERS` | `PLAINTEXT://localhost:9092` | Address that **clients** use to connect. Use `localhost` for Docker Compose, `kafka` for k8s |
| `KAFKA_CONTROLLER_QUORUM_VOTERS` | `1@localhost:9093` | Raft quorum: `nodeId@host:port` |
| `KAFKA_CONTROLLER_LISTENER_NAMES` | `CONTROLLER` | Which listener handles Raft traffic |
| `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP` | `PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT` | Maps listener names to protocols |
| `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` | `1` | Single-node — must be 1, otherwise Kafka waits for more replicas |
| `KAFKA_AUTO_CREATE_TOPICS_ENABLE` | `true` | Kafka creates a topic on first publish (no manual setup needed) |

> **`KAFKA_ADVERTISED_LISTENERS` is the most important variable.**
> It is the address Kafka tells clients to connect to after the initial handshake.
> - Docker Compose: `localhost:9092` (Spring Boot runs on the host)
> - Kubernetes: `kafka:9092` (Spring Boot pods resolve the `kafka` service name)

### Start

```bash
docker-compose -f docker-compose-infra.yaml up -d kafka kafka-ui
```

---

## 3. Kubernetes (Minikube)

### StatefulSet

```yaml
# k8s/base/infra/kafka/statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: kafka
spec:
  serviceName: kafka
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    spec:
      containers:
        - name: kafka
          image: apache/kafka:3.9.0
          env:
            - name: KAFKA_ADVERTISED_LISTENERS
              value: "PLAINTEXT://kafka:9092"   # ← use service name, not localhost
            # ... other vars same as docker-compose
```

Key difference from Docker Compose: `KAFKA_ADVERTISED_LISTENERS` uses the k8s Service name
`kafka` instead of `localhost`, because other pods resolve it via DNS.

### Service (ClusterIP)

```yaml
# k8s/base/infra/kafka/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: kafka
spec:
  selector:
    app: kafka
  ports:
    - name: broker
      port: 9092
      targetPort: 9092
  type: ClusterIP
```

### Kafka UI

```yaml
# k8s/base/infra/kafka/kafka-ui-deployment.yaml
env:
  - name: KAFKA_CLUSTERS_0_NAME
    value: "local"
  - name: KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS
    value: "kafka:9092"
  - name: SERVER_SERVLET_CONTEXT_PATH
    value: "/kafka-ui"           # ← required when serving under a sub-path
```

> **`SERVER_SERVLET_CONTEXT_PATH` is required for sub-path ingress.**
> Without it, Kafka UI generates asset URLs like `/assets/index.js` (root-relative).
> The browser requests `shopbuddy.com/assets/index.js` — no ingress rule matches → 404.
> Setting the context path makes it generate `/kafka-ui/assets/index.js` instead.

### Kafka UI Ingress

```yaml
# k8s/base/infra/kafka/kafka-ui-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kafka-ui-ingress
spec:
  rules:
    - host: "shopbuddy.com"
      http:
        paths:
          - pathType: Prefix
            path: "/kafka-ui"
            backend:
              service:
                name: kafka-ui
                port:
                  number: 8080
```

> **Do not use a regex path + `rewrite-target` here.**
> The nginx admission webhook rejects regex paths with `pathType: Prefix`.
> Regex paths require `pathType: ImplementationSpecific`, but rewriting the path means the app
> receives `/` and generates root-relative asset URLs — which then 404.
> The `SERVER_SERVLET_CONTEXT_PATH` approach avoids rewriting entirely.

---

## 4. Spring Boot — Producer (payment-service)

### Gradle Dependency

```gradle
implementation 'org.springframework.kafka:spring-kafka'
```

No version needed — Spring Boot's dependency management provides it.

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false   # ← do not embed Java class name in message headers
```

> **`spring.json.add.type.headers: false`**
> By default, `JsonSerializer` adds a `__TypeId__` header containing the fully-qualified Java
> class name. The consumer then uses this to deserialize back to the correct type. However, if
> producer and consumer are in different services (different JARs), the class names must match
> exactly. Disabling type headers and instead specifying `spring.json.value.default.type` on
> the consumer side is simpler and more decoupled.

### Publisher Bean

```java
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    public static final String TOPIC = "payment.result";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publish(PaymentEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event);
    }
}
```

`KafkaTemplate<String, PaymentEvent>` is auto-configured by Spring Boot using the
`application.yml` serializer settings. The key is the `orderId` as a String — this ensures all
events for the same order go to the same partition (ordering guarantee per order).

---

## 5. Spring Boot — Consumer (order-service)

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: order-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "dev.agasen.api.event"
        spring.json.value.default.type: "dev.agasen.api.event.PaymentEvent"
```

### Property Reference

| Property | Value | Why |
|---|---|---|
| `group-id` | `order-service` | Consumer group name. Kafka tracks offsets per group. All instances of order-service share this group |
| `auto-offset-reset` | `earliest` | On first startup (no committed offset yet), start reading from the beginning of the topic rather than only new messages |
| `key-deserializer` | `StringDeserializer` | Keys are plain strings |
| `value-deserializer` | `JsonDeserializer` | Deserialize JSON payload to a Java object |
| `spring.json.trusted.packages` | `dev.agasen.api.event` | Security whitelist — JsonDeserializer refuses to instantiate classes from untrusted packages |
| `spring.json.value.default.type` | `dev.agasen.api.event.PaymentEvent` | Which class to deserialize to when there is no `__TypeId__` header (because producer set `add.type.headers=false`) |

### Listener

```java
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderCommandService orderCommandService;

    @KafkaListener(topics = "payment.result", groupId = "order-service")
    public void onPaymentResult(PaymentEvent event) {
        String newStatus = switch (event.status()) {
            case "CAPTURED" -> "CONFIRMED";
            case "FAILED"   -> "PAYMENT_FAILED";
            default         -> null;
        };
        if (newStatus != null) {
            orderCommandService.updateStatus(event.orderId(), newStatus);
        }
    }
}
```

Spring Boot auto-detects `@KafkaListener` beans and wires them using the consumer config in
`application.yml`. No additional `@EnableKafka` is needed (Spring Boot's auto-configuration
handles it).

---

## 6. Kafka UI

### Accessing

| Environment | URL |
|---|---|
| Docker Compose | `http://localhost:9090` |
| Kubernetes | `http://shopbuddy.com/kafka-ui` |

### What to Look For

**Topics tab** — `payment.result` appears here after the first payment is submitted.
Kafka creates it automatically because `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`.

**Messages tab** — Browse individual messages. Each message looks like:

```json
{
  "orderId": 251,
  "paymentId": 3,
  "userId": "user-abc",
  "amount": 49.99,
  "status": "CAPTURED",
  "failureReason": null
}
```

**Consumer Groups tab** — Find `order-service`. The **Lag** column shows how many messages
the consumer is behind. Lag = 0 means the order-service has consumed all events. Lag > 0 means
it is behind (e.g. service is down or slow).

---

## 7. Common Pitfalls

### `KAFKA_ADVERTISED_LISTENERS` mismatch

The single most common Kafka connection failure. The advertised listener is what Kafka tells
clients to connect to _after_ the initial bootstrap. If it is set to `localhost:9092` inside
k8s, pods will try to connect to their own loopback instead of the Kafka broker.

| Environment | Correct value |
|---|---|
| Docker Compose (Spring Boot on host) | `PLAINTEXT://localhost:9092` |
| Kubernetes | `PLAINTEXT://kafka:9092` |

### `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` must be 1 on single-node

Kafka's internal `__consumer_offsets` topic defaults to replication factor 3. With a single
broker, it cannot replicate to 3 nodes and will hang indefinitely. Always set this to `1` for
local/single-node setups.

### `spring.json.trusted.packages` required

`JsonDeserializer` will throw `IllegalArgumentException` and refuse to deserialize if the target
class is not in a trusted package. Either whitelist the specific package or use `*` (not
recommended in production).

### Topic not appearing in Kafka UI

If `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`, the topic is created on first message publish.
If the payment-service hasn't processed a payment yet, the topic won't exist yet — this is
normal.

### Consumer Lag stuck / not consuming

Check that:
1. `spring.json.value.default.type` matches the exact fully-qualified class name of the event record
2. `spring.json.trusted.packages` includes the package of that class
3. The consumer's `bootstrap-servers` resolves to the actual Kafka broker
