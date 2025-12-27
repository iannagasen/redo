# Running the whole app

using k8s and docker compose

```bash
make
```

# Ports

| App                            | Local | Docker (Mapped) | Kubernetes      |
|--------------------------------|-------|-----------------|-----------------|
| StoreFront (Front End Channel) | 3000  | 8000            | -               |
| Product Service                | 8081  | 8080            | -               | 
| API Gateway                    | 8000  | 8080            | 8000 (NodePort) |
| Auth Server                    | 8080  | 8080            | 8080            |

k8s enpoints: using host (shopbuddy.com)
/product/actuator, /product/actuator/{health,info}
/product/public/hello
/swagger

temporarily disable the otlp logging by removing this in the environment variable

```text
-javaagent:src/backend/core/product-service/build/agent/opentelemetry-javaagent.jar
-Dotel.traces.exporter=logging
-Dotel.metrics.exporter=logging
-Dotel.logs.exporter=logging
```