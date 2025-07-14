package dev.agasen.core.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigurationProperties( prefix = "oauth2" )
public record OAuth2ClientProperties(
      Map< String, ClientConfig > clients
) {

   @ConstructorBinding
   public OAuth2ClientProperties {
      clients = clients != null
            ? Map.copyOf( clients )
            : Map.of();
   }

   public record ClientConfig(
         String clientId,
         String clientSecret,
         Optional< String > clientAuthenticationMethod,
         Optional< String > authorizationGrantType,
         List< String > scopes,
         TokenConfig token
   ) {

      public ClientConfig {
         scopes = scopes != null ? List.copyOf( scopes ) : List.of();
      }

      public String getClientAuthenticationMethodOrDefault() {
         return clientAuthenticationMethod.orElse( "client_secret_basic" );
      }

      public String getAuthorizationGrantTypeOrDefault() {
         return authorizationGrantType.orElse( "client_credentials" );
      }

      public TokenConfig getToken() {
         return new TokenConfig( Duration.ofMinutes( 30 ) );
      }
   }

   public record TokenConfig( Duration accessTtl ) {

      public TokenConfig {
         if ( accessTtl == null ) {
            accessTtl = Duration.ofMinutes( 30 );
         }
      }
   }
}
