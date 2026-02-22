package dev.agasen.core.product.infrastructure.openapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties( prefix = "auth" )
public class AuthProperties {

   private String authorizationUrl;
   private String tokenUrl;

}