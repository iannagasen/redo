FROM eclipse-temurin:24 as builder
WORKDIR extracted
ADD ./build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:24
WORKDIR application
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8080
EXPOSE 5005

# Use shell form to allow environment variable substitution
ENTRYPOINT sh -c "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"
