package dev.agasen.core.user.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

   @Value( "${internal.api.key}" )
   private String internalApiKey;

   @Value( "${env.base.url.internal.auth}" )
   private String authServerUrl;

   // only looks for /internal path, and will use internal authentication
   @Bean
   @Order( 1 )
   public SecurityFilterChain internalFilterChain( HttpSecurity http ) throws Exception {
      http.securityMatcher( "/internal/**" )
         .addFilterBefore( ( request, response, chain ) -> {
            HttpServletRequest req = ( HttpServletRequest ) request;
            log.info( "🔐 INTERNAL CHAIN: {} {}", req.getMethod(), req.getRequestURI() );
            chain.doFilter( request, response );
         }, UsernamePasswordAuthenticationFilter.class )
         .csrf( AbstractHttpConfigurer::disable )
         .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
         .addFilterBefore( ( request, response, chain ) -> {
            HttpServletRequest req = ( HttpServletRequest ) request;
            HttpServletResponse res = ( HttpServletResponse ) response;
            String apiKey = req.getHeader( "X-Internal-Api-Key" );
            if ( internalApiKey.equals( apiKey ) ) {
               PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(
                  "internal-service", null, Collections.singletonList( new SimpleGrantedAuthority( "ROLE_INTERNAL" ) ) );
               SecurityContextHolder.getContext().setAuthentication( auth );
               chain.doFilter( request, response );
            } else {
               log.warn( "🚫 Invalid Internal API Key for request: {}", req.getRequestURI() );
               res.sendError( 401, "Invalid Internal API Key" );
            }
         }, UsernamePasswordAuthenticationFilter.class )
         .authorizeHttpRequests( auth -> auth.anyRequest().authenticated() );

      return http.build();
   }

   // For public
   @Bean
   @Order( 2 )
   public SecurityFilterChain publicFilterChain( HttpSecurity http ) throws Exception {
      http.securityMatcher(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
         )
         .addFilterBefore( ( request, response, chain ) -> {
            HttpServletRequest req = ( HttpServletRequest ) request;
            log.info( "🟢 PUBLIC CHAIN: {} {}", req.getMethod(), req.getRequestURI() );
            chain.doFilter( request, response );
         }, UsernamePasswordAuthenticationFilter.class )
         .csrf( AbstractHttpConfigurer::disable )
         .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
         .authorizeHttpRequests( auth -> auth.anyRequest().permitAll() );

      return http.build();
   }

   // Standard (catch-all), will use OAuth
   @Bean
   @Order( 3 )
   public SecurityFilterChain standardFilterChain( HttpSecurity http ) throws Exception {
      http.addFilterBefore( ( request, response, chain ) -> {
            HttpServletRequest req = ( HttpServletRequest ) request;
            log.info( "🛡️ STANDARD (OAUTH) CHAIN: {} {}", req.getMethod(), req.getRequestURI() );
            chain.doFilter( request, response );
         }, UsernamePasswordAuthenticationFilter.class )
         .csrf( AbstractHttpConfigurer::disable )
         .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
         .authorizeHttpRequests( auth -> auth.anyRequest().authenticated() )
         .oauth2ResourceServer( oauth2 -> oauth2.jwt( jwt -> jwt.jwkSetUri( authServerUrl + "/oauth2/jwks" ) ) );

      return http.build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }
}
