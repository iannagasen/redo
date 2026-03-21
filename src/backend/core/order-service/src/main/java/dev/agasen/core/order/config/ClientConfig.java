package dev.agasen.core.order.config;

import dev.agasen.api.payment.PaymentApi;
import dev.agasen.api.product.ProductApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

   @Bean
   public ProductApi productServiceRestClient(
      @Value( "${env.base.url.internal.product}" ) String baseUrl
   ) {
      var restClient = RestClient.builder()
         .baseUrl( baseUrl + "/product" )
         .build();
      return HttpServiceProxyFactory
         .builderFor( RestClientAdapter.create( restClient ) )
         .build()
         .createClient( ProductApi.class );
   }

   @Bean
   public PaymentApi paymentServiceRestClient(
      @Value( "${env.base.url.internal.payment}" ) String baseUrl
   ) {
      var restClient = RestClient.builder()
         .baseUrl( baseUrl + "/payment" )
         .build();
      return HttpServiceProxyFactory
         .builderFor( RestClientAdapter.create( restClient ) )
         .build()
         .createClient( PaymentApi.class );
   }
}
