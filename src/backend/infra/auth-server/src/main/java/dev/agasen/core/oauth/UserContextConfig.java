package dev.agasen.core.oauth;

import dev.agasen.common.context.user.UserIdResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class UserContextConfig {
   @Bean
   public UserIdResolver userIdResolver() {
      return request -> {
         var auth = SecurityContextHolder.getContext().getAuthentication();
         if ( auth == null || !auth.isAuthenticated() ) return Optional.empty();
         return Optional.of( auth.getName() );
      };
   }
}
