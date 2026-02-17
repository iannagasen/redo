package dev.agasen.core.product.infrastructure.rest.openapi;


import dev.agasen.core.product.infrastructure.AuthProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
   info = @Info( title = "Product API", version = "v1" )
)
@RequiredArgsConstructor
//@SecurityScheme(
//   name = "oauth2",
//   type = SecuritySchemeType.OAUTH2,
//   flows = @OAuthFlows(
//      authorizationCode = @io.swagger.v3.oas.annotations.security.OAuthFlow(
//         authorizationUrl = "${auth.authorizationUrl}",
//         tokenUrl = "${auth.tokenUrl}",
//         scopes = {
//            @OAuthScope( name = "openid", description = "OpenID scope" ),
//            @OAuthScope( name = "read", description = "Read scope" ),
//         }
//      )
//   )
//)
public class OpenApiConfig {

   private final AuthProperties authProperties;

   //   @Bean
   public OpenAPI customOpenAPI() {
      // 👇 This tells Swagger to call APIs through your gateway instead of the internal URL
      // the product is from Path=/product
      //      - id: product-service
      //      uri: ${env.base.url.product-service}
      //      predicates:
      //      - Path=/product/**
      //       filters:
      //       - RewritePath=/product/(?<segment>.*), /${segment}
      return new OpenAPI()
         .servers( List.of( new Server().url( "/product" ) ) );
   }


   @Bean
   public OpenAPI anotherCustomOpenAPI(
      @Value( "${auth.authorizationUrl}" ) String authorizationUrl,
      @Value( "${auth.tokenUrl}" ) String tokenUrl
   ) {
      return new OpenAPI()
         .components( new Components()
            .addSecuritySchemes( "oauth2", new SecurityScheme()
               .type( SecurityScheme.Type.OAUTH2 )
               .flows( new OAuthFlows()
                  .authorizationCode( new OAuthFlow()
                     .authorizationUrl( authorizationUrl )
                     .tokenUrl( tokenUrl )
                     .scopes( new Scopes()
                        .addString( "openid", "OpenID scope" )
                        .addString( "read", "Read scope" )
                     )
                  )
               )
            )
         );
   }

}
