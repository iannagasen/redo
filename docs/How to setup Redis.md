# With Docker and Kubernetes

## Step 1a: Build the server container if using docker (docker compose)

```yaml
services:
  redis:
    image: redis:latest
    container_name: redis
    ports:
      - "6379:6379"

  redis-insight:
    image: redislabs/redisinsight:latest
    ports:
      - "5540"
```

## Step 1b: Build the server, if using kubernetes

**Redis**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
spec:
  ports:
    - port: 6379
  clusterIP: None # Headless service for stateful set
  selector:
    app: redis

---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis
spec:
  serviceName: redis
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
        - name: redis
          image: redis:latest
          ports:
            - containerPort: 6379
          volumeMounts:
            - name: redis-storage
              mountPath: /data
  volumeClaimTemplates:
    - metadata:
        name: redis-storage
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 1Gi
```

**Redis Insight**

```yaml
# Ingress - for connectivity
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: redis-gui-ingress
  annotations:
    kubernetes.io/ingress.class: "nginx"
spec:
  rules:
    #  Redis does not support path rewriting so will need to use subdomain
    - host: "redis.shopbuddy.com"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: redis-gui-service
                port:
                  number: 5540

---
# Service - to manage connection
apiVersion: v1
kind: Service
metadata:
  name: redis-gui-service
spec:
  selector:
    app: redis-insight
  ports:
    - port: 5540
      targetPort: 5540
  type: NodePort

---
# Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-insight-deployment
  labels:
    app: redis-insight
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis-insight
  template:
    metadata:
      labels:
        app: redis-insight
    spec:
      containers:
        - name: redis-insight
          image: redislabs/redisinsight:latest
          ports:
            - containerPort: 5540
          env:
            - name: "RI_REDIS_HOST"
              value: "redis" # name of the redis kubernetes service
            - name: "RI_REDIS_PORT"
              value: "6379"
```

## Step 2: Connect the app to the redis server

If using spring boot

```yaml
spring.data.redis:
  host: redis
  port: 6379
```
