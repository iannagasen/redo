package dev.agasen.infra.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

   @Bean
   public SecurityWebFilterChain springSecurityFilterChain( ServerHttpSecurity http ) {
      http
            .authorizeExchange( exchanges -> exchanges
//                  .pathMatchers(
//                        "/product-service/swagger-ui.html",
//                        "/product-service/swagger-ui/**",
//                        "/product-service/v3/api-docs/**",
//                        "/product-service/webjars/**"
//                  ).permitAll()
                        .anyExchange().permitAll()
            )
            .oauth2ResourceServer( oauth2 -> oauth2.jwt() );

      return http.build();
   }
}
