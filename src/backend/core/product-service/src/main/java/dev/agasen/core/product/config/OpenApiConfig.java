package dev.agasen.core.product.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

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
}
