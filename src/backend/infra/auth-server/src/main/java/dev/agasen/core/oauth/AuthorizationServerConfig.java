package dev.agasen.core.oauth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

   @Bean
   @Order( 1 )
   public SecurityFilterChain authorizationServerSecurityFilterChain( HttpSecurity http ) throws Exception {
      OAuth2AuthorizationServerConfiguration.applyDefaultSecurity( http );

      http.getConfigurer( OAuth2AuthorizationServerConfigurer.class )
         .oidc( Customizer.withDefaults() );

      http.cors( Customizer.withDefaults() )
         .exceptionHandling( exceptions -> exceptions
            .defaultAuthenticationEntryPointFor(
               new LoginUrlAuthenticationEntryPoint( "/login" ),
               new MediaTypeRequestMatcher( MediaType.TEXT_HTML )
            )
            .defaultAuthenticationEntryPointFor(
               new HttpStatusEntryPoint( HttpStatus.UNAUTHORIZED ),
               new MediaTypeRequestMatcher( MediaType.APPLICATION_JSON )
            )
         );

      http.formLogin()
         .loginPage( "/login" )
         .defaultSuccessUrl( "/home", true ); // always go here after login

      return http.build();
   }

   @Bean
   @Order( 2 )
   public SecurityFilterChain defaultSecurityFilterChain( HttpSecurity http ) throws Exception {
      http.authorizeHttpRequests( authorize -> authorize
               .requestMatchers( "/oauth2/**" ).permitAll()
               .requestMatchers( "/.well-known/appspecific/**" ).permitAll()
//
//                  // for the angular to introspect the token if it is still valid
               .requestMatchers( "/oauth2/introspect" ).permitAll()
               .anyRequest().authenticated()
         )
         .cors( Customizer.withDefaults() )
         .formLogin( Customizer.withDefaults() );

      return http.build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

   @Bean
   public ImmutableJWKSet< SecurityContext > jwkSource() {
      KeyPair keyPair = generateRsaKey();
      RSAPublicKey publicKey = ( RSAPublicKey ) keyPair.getPublic();
      RSAPrivateKey privateKey = ( RSAPrivateKey ) keyPair.getPrivate();
      RSAKey rsaKey = new RSAKey.Builder( publicKey )
         .privateKey( privateKey )
         .keyID( UUID.randomUUID().toString() )
         .build();
      JWKSet jwkSet = new JWKSet( rsaKey );
      return new ImmutableJWKSet<>( jwkSet );
   }

   private static KeyPair generateRsaKey() {
      KeyPair keyPair;
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( "RSA" );
         keyPairGenerator.initialize( 2048 );
         keyPair = keyPairGenerator.generateKeyPair();
      } catch ( Exception ex ) {
         throw new IllegalStateException( ex );
      }
      return keyPair;
   }

   @Bean
   public JwtDecoder jwtDecoder( JWKSource< SecurityContext > jwkSource ) {
      return OAuth2AuthorizationServerConfiguration.jwtDecoder( jwkSource );
   }

   @Bean
   public AuthorizationServerSettings authorizationServerSettings( @Value( "${env.base.url.internal.auth}" ) String authServerUrl ) {
      return AuthorizationServerSettings.builder()
         .issuer( authServerUrl )
         .build();
   }

   @Bean
   public OAuth2TokenCustomizer< JwtEncodingContext > jwtCustomizer() {
      return context -> {
         if ( context.getTokenType().equals( OAuth2TokenType.ACCESS_TOKEN ) ) {
            Authentication principal = context.getPrincipal();
            var authorities = principal.getAuthorities();

            List< String > roles = authorities.stream()
               .map( GrantedAuthority::getAuthority )
               .filter( a -> a.startsWith( "ROLE_" ) )
               .map( a -> a.substring( 5 ) )
               .collect( Collectors.toList() );

            List< String > permissions = authorities.stream()
               .map( GrantedAuthority::getAuthority )
               .filter( a -> !a.startsWith( "ROLE_" ) )
               .collect( Collectors.toList() );

            context.getClaims().claims( claims -> {
               if ( !roles.isEmpty() ) {
                  claims.put( "roles", roles );
               }
               if ( !permissions.isEmpty() ) {
                  claims.put( "permissions", permissions );
               }
            } );
         }
      };
   }

   @Bean
   public CorsConfigurationSource corsConfigurationSource(
      @Value( "${env.base.url.internal.product}" ) String productService,
      @Value( "${env.base.url.external.storefront}" ) String storefront
   ) {
      CorsConfiguration config = new CorsConfiguration();
//      config.setAllowedOrigins( List.of( "http://localhost:8081", "http://localhost:4200" ) );
      config.setAllowedOrigins(
         List.of(
            productService,
            storefront,
            "shopbuddy.com",
            "http://shopbuddy.com",
            "https://shopbuddy.com",
            "http://localhost:4200",// adding this for ease of local development ...
            "http://localhost:4200/storefront"
         ) );
      config.setAllowedMethods( List.of( "GET", "POST", "PUT", "DELETE", "OPTIONS" ) );
      config.setAllowedHeaders( List.of( "*" ) );
      config.setAllowCredentials( true );

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration( "/**", config );
      return source;
   }
}
