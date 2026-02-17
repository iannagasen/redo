# Kubernetes Gotchas and Troubleshooting

## 1. PostgreSQL init scripts only run once

**Problem:** `docker-entrypoint-initdb.d` scripts (mounted via ConfigMap `postgres-init-sql`) only execute when the data directory is empty — i.e., on the very first pod start. If the PVC (`postgres-storage-postgres-0`) already has data, the init SQL is silently skipped.

**Symptom:** A new database (e.g., `user_management`) doesn't exist even though `init.sql` creates it.

**Fix:** Use an **initContainer** on the deployment that needs the database:

```yaml
initContainers:
  - name: ensure-db
    image: postgres:17-alpine
    command: ['sh', '-c']
    args:
      - |
        until pg_isready -h postgres -U product_user; do echo "Waiting for postgres..."; sleep 2; done
        psql -h postgres -U product_user -d product_catalog -tc "SELECT 1 FROM pg_database WHERE datname = 'user_management'" | grep -q 1 \
          || psql -h postgres -U product_user -d product_catalog -c "CREATE DATABASE user_management;" || true
    env:
      - name: PGPASSWORD
        value: "product_password"
```

This is idempotent — safe to run every time the pod starts.

**Alternative (nuclear):** Delete the PVC and let Postgres reinitialize:

```bash
kubectl delete pvc postgres-storage-postgres-0
kubectl delete pod postgres-0
```

---

## 2. Services must set DB_HOST for Postgres in K8s

**Problem:** Spring Boot defaults `DB_HOST` to `localhost`, which doesn't work inside a K8s pod — Postgres runs in a separate pod.

**Symptom:** `Connection to localhost:5432 refused` or `Unable to determine Dialect without JDBC metadata`.

**Fix:** Set `DB_HOST` and `DB_PORT` in the deployment env:

```yaml
env:
  - name: DB_HOST
    value: "postgres"
  - name: DB_PORT
    value: "5432"
```

The value `postgres` matches the K8s Service name defined in `k8s/base/infra/postgres/service.yaml`.

---

## 3. Java version mismatch in Dockerfiles

**Problem:** The Gradle toolchain compiles with Java 24 (`languageVersion = JavaLanguageVersion.of(24)`), but the Dockerfile runtime stage uses an older JRE.

**Symptom:** `UnsupportedClassVersionError: ... has been compiled by a more recent version of the Java Runtime (class file version 68.0), this version of the Java Runtime only recognizes class file versions up to 65.0`

**Fix:** Ensure both stages of the Dockerfile use the same Java version:

```dockerfile
FROM eclipse-temurin:24 as builder
# ...
FROM eclipse-temurin:24    # Must match the compile version
```

**Class file version reference:** Java 21 = 65, Java 22 = 66, Java 23 = 67, Java 24 = 68.

---

## 4. Auth-server depends on user-service schema

The auth-server uses raw JDBC (`JdbcUserDetailsService`) to query the `users`, `roles`, `user_role`, `permission`, and `role_permission` tables. These tables are created by the **user-service** via Liquibase migrations.

**Key point:** The auth-server boots fine without the tables — it only queries them at login time. As long as user-service starts and Liquibase runs before the first login attempt, everything works. No need to block auth-server startup waiting for tables.

---

## 5. Deleting all K8s resources

To tear down everything deployed by Skaffold:

```bash
skaffold delete
```

To manually delete all resources with kubectl:

```bash
kubectl delete all --all
kubectl delete configmap --all
kubectl delete ingress --all
kubectl delete pvc --all
kubectl delete clusterissuer --all
```

Or nuke an entire namespace:

```bash
kubectl delete namespace <namespace-name>
```

---

## 6. envFrom for shared config

Services that need to resolve other services' internal URLs should reference the shared ConfigMap:

```yaml
envFrom:
  - configMapRef:
      name: backend-url-configmap
```

This provides `INTERNAL_AUTH_SERVER_BASE_URL`, `INTERNAL_PRODUCT_SERVICE_BASE_URL`, etc. Defined in `k8s/base/shared/secrets/backend-url-configmap.yaml`.
