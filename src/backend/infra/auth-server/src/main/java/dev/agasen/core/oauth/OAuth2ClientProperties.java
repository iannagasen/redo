package dev.agasen.core.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static dev.agasen.common.utility.CollectionsHelper.isNullOrEmpty;
import static dev.agasen.common.utility.CollectionsHelper.listOrEmpty;
import static dev.agasen.common.utility.StringHelper.blankCoalescing;

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
         String clientAuthenticationMethod,
         List< String > authorizationGrantTypes,
         List< String > redirectUris,
         List< String > scopes,
         TokenConfig token,
         ClientSettingsConfig clientSettings
   ) {

      public ClientConfig {
         scopes = scopes != null ? scopes : java.util.List.of();
         scopes.add( "read" );
      }

      public String getClientAuthenticationMethodOrDefault() {
         return blankCoalescing( clientAuthenticationMethod, "client_secret_basic" );
      }

      public List< String > getAuthorizationGrantTypesOrDefault() {
         return isNullOrEmpty( authorizationGrantTypes ) ? java.util.List.of( "authorization_code" ) : authorizationGrantTypes;
      }

      public TokenConfig getToken() {
         return new TokenConfig( Duration.ofMinutes( 30 ) );
      }

      public List< String > getRedirectUris() {
         return listOrEmpty( redirectUris );
      }

      public ClientSettingsConfig getClientSettingsConfig() {
         return clientSettings == null ? new ClientSettingsConfig( false, false ) : clientSettings;
      }
   }

   public record TokenConfig( Duration accessTtl ) {

      public TokenConfig {
         if ( accessTtl == null ) {
            accessTtl = Duration.ofMinutes( 30 );
         }
      }
   }

   public record ClientSettingsConfig(
         Boolean requireAuthorizationConsent,
         Boolean requireProofKey ) {

      public ClientSettingsConfig {
         if ( requireAuthorizationConsent == null ) {
            requireAuthorizationConsent = false;
         }

         if ( requireProofKey == null ) {
            requireProofKey = false;
         }
      }
   }
}
