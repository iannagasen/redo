package dev.agasen.core.oauth.outbound.grpc;

import dev.agasen.api.grpc.user.UserAuthServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcClientConfig {

   @Value( "${env.base.ports.grpc.user}" )
   private int grpcPort;

   @Value( "${env.base.host.grpc.user}" )
   private String userServiceGrpcHost;

   // The shutdown on destroy is important, close the connection when the app stops
   // Without this, it might leave "hanging" connection on the server side, leading to resource exhaustion
   @Bean( destroyMethod = "shutdown" )
   public ManagedChannel managedChannel() {
      return ManagedChannelBuilder
         .forAddress( userServiceGrpcHost, grpcPort )
         .usePlaintext() // no TLS for now (pod to pod k8s cluster)
         .build();
   }


   // Stub is the entire client — deadline is set per-call, not here.
   // withDeadlineAfter() starts a countdown from when it's called.
   // Setting it on the bean would expire the deadline at startup, before any call is made.
   @Bean
   public UserAuthServiceGrpc.UserAuthServiceBlockingStub userAuthServiceBlockingStub( ManagedChannel channel ) {
      return UserAuthServiceGrpc.newBlockingStub( channel );
   }
}
