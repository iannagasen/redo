Step 1 — Add versions to gradle/libs.versions.toml

Add three version entries:
grpc = "1.68.1"
protobuf-java = "3.25.3"
protobuf-plugin = "0.9.4"

Add four library entries:
grpc-netty-shaded → io.grpc:grpc-netty-shaded          (the transport layer — Netty under the hood)
grpc-protobuf → io.grpc:grpc-protobuf               (bridges Protobuf messages into gRPC)
grpc-stub → io.grpc:grpc-stub                   (the generated client/server base classes)
protobuf-java → com.google.protobuf:protobuf-java   (the message serialization runtime)

Add one plugin entry:
protobuf → com.google.protobuf   (the Gradle plugin that runs protoc at build time)

  ---
Step 2 — Update platform:api/build.gradle

This is where the .proto file lives and where code gets generated. Three changes:

1. Apply the protobuf plugin in the plugins {} block alongside the existing ones.

2. Add dependencies — grpc-protobuf, grpc-stub, and protobuf-java as implementation. The generated Java stubs reference
   these at compile time.

3. Add a protobuf {} configuration block at the bottom. This is how you tell Gradle how to run protoc:
   protobuf {
   protoc {
   artifact = "com.google.protobuf:protoc:3.25.3"   // the protoc compiler binary
   }
   plugins {
   grpc {
   artifact = "io.grpc:protoc-gen-grpc-java:1.68.1"  // the gRPC Java plugin for protoc
   }
   }
   generateProtoTasks {
   all()*.plugins {
   grpc {} // tell every task to also generate the gRPC stubs (not just messages)
   }
   }
   }

▎ What this does: Gradle downloads protoc and the gRPC codegen plugin, runs them on your .proto files, and puts the
output in build/generated/source/proto/main/. The
plugin automatically adds that path to your main source set — you never see it in src/.

  ---
Step 3 — Create the .proto file

Location: src/backend/platform/api/src/main/proto/

Create a file called user_auth.proto. Things to include:

- syntax = "proto3"; at the top
- option java_package = "dev.agasen.grpc.user"; — the Java package for generated classes
- option java_multiple_files = true; — generates one .java file per message/service instead of one giant file
- A service UserAuthService with one RPC: GetUserAuthInfo (unary — one request, one response)
- A message UserAuthInfoRequest with just a username field
- A message UserAuthInfo mirroring the fields from user.user.core.dev.agasen.platform.contracts.UserAuthInfo — id,
  username, password_hash, enabled, locked, deleted, roles
  (repeated string), permissions (repeated string)

▎ Field numbers (the = 1, = 2 after each field) must never change once deployed. Assign them starting from 1 and never
reuse a number even if you delete a field.

After you write this and run ./gradlew :src:backend:platform:platform-contracts:generateProto, check
build/generated/source/proto/main/ — you'll see the generated Java. Read it.
Understand what UserAuthServiceGrpc.UserAuthServiceImplBase looks like.

  ---
Step 4 — Update user-service/build.gradle

Add grpc-netty-shaded as implementation. This is the server transport. You also need grpc-stub and grpc-protobuf
explicitly because platform:api uses the java plugin
(not java-library), so those dependencies aren't transitive to user-service's compile classpath.

  ---
Step 5 — Create UserAuthGrpcService in user-service

Package: dev.agasen.core.user.grpc

This class:

- Extends UserAuthServiceGrpc.UserAuthServiceImplBase (the generated abstract class)
- Is a Spring @Service
- Injects UserRepository
- Overrides getUserAuthInfo(UserAuthInfoRequest request, StreamObserver<UserAuthInfo> responseObserver)

Inside the method, the pattern is always the same:

1. Do your logic
2. Call responseObserver.onNext(result)   — sends the response
3. Call responseObserver.onCompleted()   — signals end of stream
   OR on error:
   responseObserver.onError(Status.NOT_FOUND.withDescription("...").asRuntimeException())

▎ StreamObserver is the gRPC callback interface. Even in unary calls you use it. onNext sends a message, onCompleted
closes the call cleanly, onError closes it with
a gRPC status code. Think of Status.NOT_FOUND as the gRPC equivalent of HTTP 404.

The logic for building the UserAuthInfo proto response is the same as what's already in InternalUserRestService — same
repository call, same role/permission
extraction. The only difference is you're building a Protobuf message (UserAuthInfo.newBuilder()...build()) instead of a
Java record.

  ---
Step 6 — Create GrpcServerLifecycle in user-service

gRPC runs on its own port (separate from HTTP port 8080). You need to start and stop it with Spring.

Package: dev.agasen.core.user.grpc

Implement Spring's SmartLifecycle interface. This lets Spring manage the gRPC server as part of its lifecycle — starts
after the application context is ready, shuts
down cleanly on SIGTERM.

You'll need:

- A field: Server server (from io.grpc.Server)
- @Value("${grpc.server.port:9090}") for the port
- start() — calls ServerBuilder.forPort(port).addService(yourGrpcService).build().start()
- stop() — calls server.shutdown().awaitTermination(30, TimeUnit.SECONDS)
- isRunning() — returns whether the server is up

▎ ServerBuilder.forPort() is the gRPC equivalent of configuring server.port in Spring. addService() registers your
implementation — you can add multiple services to
one server.

  ---
Step 7 — Add gRPC port to user-service/application.yml

grpc:
server:
port: 9090

Convention: HTTP on 8080, gRPC on 9090.

  ---
Step 8 — Update auth-server/build.gradle

Same as user-service: add grpc-netty-shaded, grpc-stub, grpc-protobuf. auth-server needs grpc on its classpath to call
the stub.

  ---
Step 9 — Create GrpcClientConfig in auth-server

Package: dev.agasen.core.oauth

A @Configuration class with two @Bean methods:

Bean 1: ManagedChannel

- ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
- Add destroyMethod = "shutdown" to @Bean so Spring shuts it down cleanly
- Inject host/port from config via @Value

▎ usePlaintext() means no TLS. Fine for inside a K8s cluster (pod-to-pod traffic on the cluster network). For mTLS you'd
use useTransportSecurity() + cert-manager —
future work.

Bean 2: UserAuthServiceGrpc.UserAuthServiceBlockingStub

- Takes the ManagedChannel as a parameter
- Returns UserAuthServiceGrpc.newBlockingStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS)

▎ withDeadlineAfter sets a deadline — if the call hasn't completed in 5 seconds, it throws a StatusRuntimeException with
DEADLINE_EXCEEDED. This is gRPC's timeout
mechanism. Always set one on blocking stubs.

  ---
Step 10 — Add gRPC client config to auth-server/application.yml

grpc:
client:
user-service:
host: ${INTERNAL_USER_SERVICE_GRPC_HOST:user-service}
port: ${INTERNAL_USER_SERVICE_GRPC_PORT:9090}

  ---
Step 11 — Refactor ExternalUserDetailsService

Replace:

- RestTemplate injection → UserAuthServiceGrpc.UserAuthServiceBlockingStub injection
- The @Value fields for URL and API key → delete them, no longer needed
- The HTTP call body → one gRPC call: stub.getUserAuthInfo(UserAuthInfoRequest.newBuilder().setUsername(username)
  .build())
- The exception handling → catch StatusRuntimeException instead of HttpClientErrorException