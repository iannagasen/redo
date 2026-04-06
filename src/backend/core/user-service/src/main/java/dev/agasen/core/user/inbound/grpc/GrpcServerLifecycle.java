package dev.agasen.core.user.inbound.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {

   private final UserAuthGrpcService userAuthGrpcService;
   private final int port;

   private Server server;

   private volatile boolean running = false;

   public GrpcServerLifecycle( UserAuthGrpcService userAuthGrpcService, @Value( "${env.grpc.server.port:9090}" ) int port ) {
      this.userAuthGrpcService = userAuthGrpcService;
      this.port = port;
   }

   @Override
   public void start() {
      try {
         // Q: Why not use the server port 8080?
         // A: You cant because server port is HTTP1.1 and HTTP2
         server = ServerBuilder.forPort( port )
            .executor( Executors.newVirtualThreadPerTaskExecutor() )
            .addService( userAuthGrpcService )
            .build()
            .start();

         running = true;
      } catch ( IOException e ) {
         throw new RuntimeException( e );
      }
   }

   @Override
   public void stop() {
      if ( server != null ) {
         server.shutdown();

         try {
            server.awaitTermination( 30, TimeUnit.SECONDS );
         } catch ( InterruptedException e ) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
         }
      }
   }

   @Override
   public boolean isRunning() {
      return running;
   }
}
