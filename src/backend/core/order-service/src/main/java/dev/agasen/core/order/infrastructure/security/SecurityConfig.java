package dev.agasen.core.order.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity( prePostEnabled = true )
public class SecurityConfig {

   @Value( "${env.base.url.internal.auth}" ) String authServerUrl;

   @Bean
   @Order( 1 )
   public SecurityFilterChain publicFilterChain( HttpSecurity http ) throws Exception {
      http.securityMatcher(
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/public/**"
         )
         .addFilterBefore( ( request, response, chain ) -> {
            HttpServletRequest req = ( HttpServletRequest ) request;
            System.out.println( "🟢 PUBLIC CHAIN: " + req.getMethod() + " " + req.getRequestURI() );
            chain.doFilter( request, response );
         }, UsernamePasswordAuthenticationFilter.class )
         .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
         .csrf( AbstractHttpConfigurer::disable )
         .authorizeHttpRequests( authorize -> authorize.anyRequest().permitAll() )
         .oauth2ResourceServer( AbstractHttpConfigurer::disable )
         .exceptionHandling( AbstractHttpConfigurer::disable )
         .anonymous( AbstractHttpConfigurer::disable );

      return http.build();
   }

   @Bean
   @Order( 2 )
   public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
      http
         .cors( cors -> cors.configurationSource( corsConfigurationSource() ) )
         .csrf( AbstractHttpConfigurer::disable )
         .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
         .authorizeHttpRequests( authorize -> authorize.anyRequest().authenticated() )
         .oauth2ResourceServer( oauth2 -> oauth2
            .jwt( jwt -> jwt
               .jwkSetUri( authServerUrl + "/oauth2/jwks" )
               .jwtAuthenticationConverter( jwtAuthenticationConverter() )
            )
         );
      return http.build();
   }

   @Bean
   public JwtAuthenticationConverter jwtAuthenticationConverter() {
      JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
      converter.setJwtGrantedAuthoritiesConverter( jwtGrantedAuthoritiesConverter() );
      return converter;
   }

   private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
      JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

      return jwt -> {
         Collection<GrantedAuthority> authorities = new ArrayList<>( defaultConverter.convert( jwt ) );

         List<String> roles = jwt.getClaimAsStringList( "roles" );
         if ( roles != null ) {
            roles.forEach( role -> authorities.add( new SimpleGrantedAuthority( "ROLE_" + role ) ) );
         }

         List<String> permissions = jwt.getClaimAsStringList( "permissions" );
         if ( permissions != null ) {
            permissions.forEach( perm -> authorities.add( new SimpleGrantedAuthority( perm ) ) );
         }

         return authorities;
      };
   }

   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOriginPatterns( Arrays.asList( "*" ) );
      configuration.setAllowedMethods( Arrays.asList( "GET", "POST", "PUT", "DELETE", "OPTIONS" ) );
      configuration.setAllowedHeaders( Arrays.asList( "*" ) );
      configuration.setAllowCredentials( true );

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration( "/**", configuration );
      return source;
   }
}
