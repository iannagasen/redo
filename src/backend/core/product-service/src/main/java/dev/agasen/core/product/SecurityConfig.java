package dev.agasen.core.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity( prePostEnabled = true )
public class SecurityConfig {

   @Bean
   public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
      http
            .authorizeHttpRequests( authorize -> authorize
                  .requestMatchers( "/actuator/**" ).permitAll()
                  .requestMatchers( "/.well-known/**" ).permitAll()
                  .anyRequest().authenticated()
            )
            .oauth2ResourceServer( oauth2 -> oauth2
                  .jwt( jwt -> jwt
                        .jwkSetUri( "http://localhost:8080/.well-known/jwks" )
                  )
            );
      return http.build();
   }

}
