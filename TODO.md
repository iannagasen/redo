1. Missing Core Services (Features)

* Order Service: Implement the checkout logic described in FLOW.md. This should handle order creation, status
  management, and history.
* Inventory Service: Create a standalone service to manage stock levels. This prevents race conditions where two
  users buy the last item simultaneously.
* Cart Service: While the frontend can use local storage, a backend Cart Service (using Redis) is better for
  cross-device persistence and "saved for later" features.
* Payment Integration: Implement a "Mock Payment Service" or integrate with a sandbox (Stripe/PayPal) to complete the
  end-to-end checkout flow.


2. DevOps & Infrastructure

* Observability (High Priority):
    * Re-enable the OpenTelemetry agent in your product-service.
    * Deploy Jaeger or Zipkin to your K8s cluster for distributed tracing.
    * Setup Prometheus & Grafana to monitor JVM metrics and service health.
* Asynchronous Messaging: Deploy Kafka or RabbitMQ. Move the "Order Created -> Inventory Update" logic from
  synchronous REST calls to event-driven messages.
* Kustomize/Helm: Your skaffold.yaml uses rawYaml. Refactor your k8s/ directory to use Kustomize to manage
  environment-specific configurations (e.g., dev vs. prod database credentials).
* CI/CD Pipeline: Set up a GitHub Action to run ./gradlew build and linting on every push.
* start containers in sequential order
* Utilize fully Skaffold for development - fast restart
    * Currently still using `./gradlew build` then `skaffold dev`


3. Technical Debt & Enhancements

* HTTPS/TLS: Replace the dummy certificates with a proper cert-manager setup in Minikube using self-signed or Let's
  Encrypt certificates to secure shopbuddy.com.
* Inter-service Security: Implement mTLS or ensure the API Gateway is the only entry point, with internal services
  requiring internal JWT validation.
* Service Communication: Experiment with gRPC for high-performance communication between the Order and Inventory
  services instead of standard REST.
* Database Migrations: If not already using it, integrate Flyway or Liquibase into your Spring Boot services to
  manage schema changes automatically.


4. Frontend (Storefront)

* State Management: Implement a robust state management solution (like NgRx or just refined Signals) to handle the
  complex cart and user session states.
* Real-time Updates: Use WebSockets (Spring Websocket/STOMP) to push order status updates (e.g., "Processing" ->
  "Shipped") directly to the UI.
