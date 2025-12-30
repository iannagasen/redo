package dev.agasen.core.product.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
         // skip oauth resource server
         .oauth2ResourceServer( AbstractHttpConfigurer::disable )
         // Disable the exception handling that's causing the redirect
         .exceptionHandling( AbstractHttpConfigurer::disable )
         .anonymous( AbstractHttpConfigurer::disable )
      ;

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
            )
         );
      return http.build();
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
