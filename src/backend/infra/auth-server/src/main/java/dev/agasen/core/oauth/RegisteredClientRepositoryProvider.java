package dev.agasen.core.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RegisteredClientRepositoryProvider {

   private final OAuth2ClientProperties properties;
   private final PasswordEncoder passwordEncoder;
   private volatile RegisteredClientRepository registeredClientRepository;

   public RegisteredClientRepositoryProvider( OAuth2ClientProperties properties, PasswordEncoder passwordEncoder ) {
      this.properties = properties;
      this.passwordEncoder = passwordEncoder;
   }

   @Bean
   @Lazy
   public RegisteredClientRepository registeredClientRepository() {
      if ( registeredClientRepository == null ) {
         synchronized ( this ) {
            if ( registeredClientRepository == null ) {
               registeredClientRepository = createRegisteredClientRepository();
            }
         }
      }
      return registeredClientRepository;
   }

   private RegisteredClientRepository createRegisteredClientRepository() {

      List< RegisteredClient > clients = new ArrayList<>();

      properties.clients().forEach( ( clientName, config ) -> {
         RegisteredClient.Builder clientBuilder = RegisteredClient.withId( UUID.randomUUID().toString() )
               .clientId( config.clientId() )
               // PKCE
               .clientSettings( ClientSettings.builder()
                     .requireAuthorizationConsent( config.getClientSettingsConfig().requireAuthorizationConsent() )
                     .requireProofKey( config.getClientSettingsConfig().requireProofKey() )
                     .build()
               )
               .authorizationGrantTypes( grantTypes -> {
                  config.getAuthorizationGrantTypesOrDefault()
                        .stream()
                        .map( AuthorizationGrantType::new )
                        .forEach( grantTypes::add );
               } )
               .scopes( scopes -> scopes.addAll( config.scopes() ) )
               .tokenSettings( TokenSettings.builder()
                     .accessTokenTimeToLive( config.getToken().accessTtl() )
                     .build() )
               .redirectUris( redirectUris -> {
                  redirectUris.addAll( config.getRedirectUris() );
               } );

         if ( config.clientSecret() != null ) {
            clientBuilder.clientSecret( passwordEncoder.encode( config.clientSecret() ) );
         }

         if ( config.clientAuthenticationMethod() != null ) {
            clientBuilder.clientAuthenticationMethod( ClientAuthenticationMethod.valueOf( config.clientAuthenticationMethod() ) );
         }

         clients.add( clientBuilder.build() );
      } );

      return new InMemoryRegisteredClientRepository( clients );
   }
}
