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
