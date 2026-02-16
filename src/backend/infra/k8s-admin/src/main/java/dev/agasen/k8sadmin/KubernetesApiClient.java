package dev.agasen.k8sadmin;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class KubernetesApiClient {
   private static final Logger log = LoggerFactory.getLogger( KubernetesApiClient.class );

   private static final String K8S_API_SERVER_URL = "https://kubernetes.default.svc:443";
   private static final String TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

   private final RestClient restClient;

   public KubernetesApiClient( RestClientSsl restClientSsl ) {
      this.restClient = RestClient.builder()
         .baseUrl( K8S_API_SERVER_URL )
         .apply( restClientSsl.fromBundle( "k8s-bundle" ) )
         .build();
   }

   public JsonNode listPods() {
      log.debug( "Fetching pods list" );
      return get( "/api/v1/namespaces/default/pods" );
   }

   public JsonNode getPod( String name ) {
      log.debug( "Fetching pod: {}", name );
      return get( "/api/v1/namespaces/default/pods/{name}", name );
   }

   public String getPodLogs( String name ) {
      log.debug( "Fetching logs for pod: {}", name );
      return restClient.get()
         .uri( "/api/v1/namespaces/default/pods/{name}/log?tailLines=500", name )
         .header( "Authorization", buildAuthorizationHeader() )
         .retrieve()
         .body( String.class );
   }

   public JsonNode listDeployments() {
      log.debug( "Fetching deployments list" );
      return get( "/apis/apps/v1/namespaces/default/deployments" );
   }

   public JsonNode listServices() {
      log.debug( "Fetching services list" );
      return get( "/api/v1/namespaces/default/services" );
   }

   public JsonNode listConfigMaps() {
      log.debug( "Fetching configmaps list" );
      return get( "/api/v1/namespaces/default/configmaps" );
   }

   private JsonNode get( String uri, Object... uriVariables ) {
      return restClient.get()
         .uri( uri, uriVariables )
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
}
