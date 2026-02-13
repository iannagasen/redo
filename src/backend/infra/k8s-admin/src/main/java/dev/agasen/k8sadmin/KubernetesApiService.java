package dev.agasen.k8sadmin;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class KubernetesApiService {
   private static final Logger log = LoggerFactory.getLogger( KubernetesApiService.class );

   private static final String K8S_API_SERVER_URL = "https://kubernetes.default.svc:443";
   private static final String TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";
   private static final String CA_CERT_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";

   private final RestClient restClient;

   public KubernetesApiService( RestClientSsl restClientSsl ) {
      this.restClient = RestClient.builder()
         .baseUrl( K8S_API_SERVER_URL )
         // see k8s-bundle in application.yaml
         .apply( restClientSsl.fromBundle( "k8s-bundle" ) )
         .build();
   }

   @GetMapping( "/hello" )
   public String hello() {
      log.info( ">>>> Hello World! called" );
      return "Hello World";
   }

   @GetMapping( "/pods" )
   public JsonNode listPods() {
      log.info( ">>>> listPods called" );

      return restClient.get()
         .uri( "/api/v1/namespaces/default/pods" )
         .header( "Authorization", buildAuthorizationHeader() )
         .retrieve()
         .body( JsonNode.class );
   }


   // TODO: handle token expiration
   private String buildAuthorizationHeader() {
      try {
         String token = Files.readString( Paths.get( TOKEN_PATH ) ).trim();
         return "Bearer " + token;
      } catch ( Exception e ) {
         throw new RuntimeException( e );
      }
   }

   // Example: Get a specific pod
   @GetMapping( "/pods/{podName}" )
   public JsonNode getPod( String podName ) {
      return restClient.get()
         .uri( "/api/v1/namespaces/default/pods/{podName}", podName )
         .header( "Authorization", buildAuthorizationHeader() )
         .retrieve()
         .body( JsonNode.class );
   }

   // Example: List deployments
   @GetMapping( "/deployments" )
   public JsonNode listDeployments() {
      JsonNode response = restClient.get()
         .uri( "/apis/apps/v1/namespaces/default/deployments" )
         .header( "Authorization", buildAuthorizationHeader() )
         .retrieve()
         .body( JsonNode.class );

      return response;
   }
}
