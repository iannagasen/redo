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

http://shopbuddy.com/storefront

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