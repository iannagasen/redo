# How to add and enable OpenTelemetry in a single service

## 1. Update `build.gradle`

### Add a configuration for the agent

```groovy
configurations {
    agent
}
```

### Add dependency for the OTel Java agent

```groovy
dependencies {
    agent "io.opentelemetry.javaagent:2.20.0"
}
```

*(This pulls the agent jar from Maven Central.)*

### Add a task to copy and rename the agent jar

```groovy
tasks.register('copyAgent', Copy) {
    from configurations.agent {
        rename "opentelemetry-javaagent-.*\\.jar", "opentelemetry-javaagent.jar"
    }
    into layout.buildDirectory.dir("agent")
}
```

### Make sure the jar is copied before building

```groovy
bootJar {
    dependsOn copyAgent
    archiveFileName = "app.jar"
}
```

---

## 2. Run configuration

In IntelliJ or command line, add **VM options**:

```bash
-javaagent:build/agent/opentelemetry-javaagent.jar
-Dotel.traces.exporter=logging
-Dotel.metrics.exporter=logging
-Dotel.logs.exporter=logging
```

⚠️ Important: The `-javaagent` path is **relative to the working directory**.  
If you set IntelliJ’s working directory to the module root (`src/backend/core/product-service`), then
`build/agent/opentelemetry-javaagent.jar` works.  
If you leave IntelliJ at the project root, you’ll need to use the full path:

```bash
-javaagent=src/backend/core/product-service/build/agent/opentelemetry-javaagent.jar
```

---

✅ With this setup:

- Gradle always downloads/copies the agent before packaging your app.
- IntelliJ and CLI runs both pick up the agent correctly if the working directory is set properly.
- Exporters are set to `logging`, so spans, metrics, and logs go to the console.
