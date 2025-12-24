package dev.agasen.core.product.infrastructure.rest.openapi;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
   info = @Info( title = "Product API", version = "v1" )
)
@SecurityScheme(
   name = "oauth2",
   type = SecuritySchemeType.OAUTH2,
   flows = @OAuthFlows(
      authorizationCode = @io.swagger.v3.oas.annotations.security.OAuthFlow(
         authorizationUrl = "http://localhost:8080/oauth2/authorize",
         tokenUrl = "http://localhost:8080/oauth2/token",
         scopes = {
            @OAuthScope( name = "openid", description = "OpenID scope" ),
            @OAuthScope( name = "read", description = "Read scope" ),
         }
      )
   )
)
public class OpenApiConfig {
   @Bean
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

}
