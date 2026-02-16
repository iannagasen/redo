# How to resolve - CORS with Kubernetes Ingress

## The Problem

When an Angular app running on `http://localhost:4200` makes API calls to a backend behind a Kubernetes Ingress (e.g. `http://shopbuddy.com/k8s-admin/pods`), the browser blocks the request:

```
Access to fetch at 'http://shopbuddy.com/k8s-admin/services' from origin 'http://localhost:4200'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the
requested resource.
```

## Why Spring's CORS config alone doesn't work

You might already have a `WebMvcConfigurer` in your Spring Boot app:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
   @Override
   public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
         .allowedOrigins("http://localhost:4200")
         .allowedMethods("*")
         .allowedHeaders("*");
   }
}
```

This works when calling the backend directly (e.g. `http://localhost:8080`), but **fails when the request goes through the nginx ingress controller**. The reason:

1. The browser sends a **preflight `OPTIONS` request** before the actual `GET`/`POST`.
2. The **nginx ingress controller intercepts** the `OPTIONS` request and responds immediately — it never forwards it to the Spring backend.
3. The response has no `Access-Control-Allow-Origin` header, so the browser blocks the subsequent request.

## The Fix

Add CORS annotations to the Ingress resource so nginx handles CORS at the edge:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-k8s-admin
  annotations:
    nginx.ingress.kubernetes.io/enable-cors: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "http://localhost:4200"
    nginx.ingress.kubernetes.io/cors-allow-methods: "GET, POST, OPTIONS"
    nginx.ingress.kubernetes.io/cors-allow-headers: "Content-Type, Authorization"
spec:
  rules:
    - host: "shopbuddy.com"
      http:
        paths:
          - pathType: Prefix
            path: "/k8s-admin"
            backend:
              service:
                name: k8s-admin-service
                port:
                  number: 8080
```

Apply the change:

```bash
kubectl apply -f k8s/base/ingress-k8s-admin.yaml
```

## Key annotations explained

| Annotation | Purpose |
|---|---|
| `enable-cors: "true"` | Enables nginx CORS handling for this ingress |
| `cors-allow-origin` | Allowed origin(s). Use `*` for any, or a specific origin like `http://localhost:4200` |
| `cors-allow-methods` | HTTP methods allowed in CORS requests |
| `cors-allow-headers` | Headers the client is allowed to send |

## Multiple origins

To allow multiple origins (e.g. local dev + deployed frontend):

```yaml
nginx.ingress.kubernetes.io/cors-allow-origin: "http://localhost:4200, http://shopbuddy.com"
```

## When you don't need the ingress fix

If your frontend and backend are on the **same origin** (same host + port), CORS is not an issue. This is the case when both are served behind the same ingress host (e.g. both at `shopbuddy.com`). The fix is only needed for **cross-origin** scenarios like local Angular dev (`localhost:4200`) calling the K8s-hosted backend (`shopbuddy.com`).
