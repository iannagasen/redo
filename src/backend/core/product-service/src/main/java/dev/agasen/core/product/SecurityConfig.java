package dev.agasen.core.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity( prePostEnabled = true )
public class SecurityConfig {

   @Bean
   public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
      http
            .cors( cors -> cors.configurationSource( corsConfigurationSource() ) )
            .csrf( AbstractHttpConfigurer::disable )
            .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
            .authorizeHttpRequests( authorize -> authorize
                  .requestMatchers( "/actuator/**" ).permitAll()
                  .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                  ).permitAll()
                  .anyRequest().authenticated()
            )
            .oauth2ResourceServer( oauth2 -> oauth2
                  .jwt( jwt -> jwt
                        .jwkSetUri( "http://localhost:8080/oauth2/jwks" )
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
