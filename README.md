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

