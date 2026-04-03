package dev.agasen.core.order.outbound.client;

import dev.agasen.api.core.payment.PaymentApi;
import dev.agasen.api.core.product.ProductApi;
import dev.agasen.common.context.user.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ProductServiceRestClient {

   @Bean
   public ProductApi paymentServiceRestClient(
      @Value( "${env.base.url.internal.product}" ) String baseUrl
   ) {
      var restClient = RestClient.builder()
         .baseUrl( baseUrl )
         .requestInterceptor( bearerTokenInterceptor() )
         .build();
      return HttpServiceProxyFactory
         .builderFor( RestClientAdapter.create( restClient ) )
         .build()
         .createClient( ProductApi.class );
   }

   private ClientHttpRequestInterceptor bearerTokenInterceptor() {
      return ( request, body, execution ) -> {
         if ( UserContext.isTokenBound() ) {
            request.getHeaders().setBearerAuth( UserContext.currentToken() );
         }
         return execution.execute( request, body );
      };
   }
}
