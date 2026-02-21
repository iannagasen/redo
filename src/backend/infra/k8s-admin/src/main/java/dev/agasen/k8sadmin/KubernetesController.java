package dev.agasen.k8sadmin;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KubernetesController {
   private static final Logger log = LoggerFactory.getLogger( KubernetesController.class );

   private final KubernetesApiClient k8sClient;

   public KubernetesController( KubernetesApiClient k8sClient ) {
      this.k8sClient = k8sClient;
   }

   @GetMapping( "/hello" )
   public String hello() {
      log.info( ">>>> Hello World! called" );
      return "Hello World";
   }

   @GetMapping( "/pods" )
   public JsonNode listPods() {
      log.info( ">>>> listPods called" );
      return k8sClient.listPods();
   }

   @GetMapping( "/pods/{podName}" )
   public JsonNode getPod( @PathVariable String podName ) {
      log.info( ">>>> getPod called for: {}", podName );
      return k8sClient.getPod( podName );
   }

   @GetMapping( value = "/pods/{podName}/logs", produces = "text/plain" )
   public String getPodLogs( @PathVariable String podName ) {
      log.info( ">>>> getPodLogs called for: {}", podName );
      return k8sClient.getPodLogs( podName );
   }

   @GetMapping( "/deployments" )
   public JsonNode listDeployments() {
      log.info( ">>>> listDeployments called" );
      return k8sClient.listDeployments();
   }

   @GetMapping( "/services" )
   public JsonNode listServices() {
      log.info( ">>>> listServices called" );
      return k8sClient.listServices();
   }

   @GetMapping( "/configmaps" )
   public JsonNode listConfigMaps() {
      log.info( ">>>> listConfigMaps called" );
      log.info( "Hello Ian" );
      return k8sClient.listConfigMaps();
   }
}
