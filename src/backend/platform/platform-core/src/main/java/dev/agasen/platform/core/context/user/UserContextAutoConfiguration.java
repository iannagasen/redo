package dev.agasen.platform.core.context.user;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that registers {@link UserContextBinder} as a servlet filter.
 *
 * <p>This configuration is loaded automatically via Spring Boot's auto-configuration
 * mechanism for any application that has {@code platform:common} on its classpath.
 *
 * <p>The {@link UserContextBinder} bean is only created when a {@link UserIdResolver} bean
 * is present. Services that use {@link UserContext} must provide one — typically by extending
 * {@link UserContextConfiguration}. Services that do not use {@link UserContext} are unaffected.
 *
 * <p>If a service uses {@link UserContext} features but forgets to extend
 * {@link UserContextConfiguration}, the filter is simply absent and user IDs will not be bound,
 * which surfaces as a runtime error in the first request that calls {@link UserContext#currentUserId()}.
 */
@AutoConfiguration
public class UserContextAutoConfiguration {

   @Bean
   @ConditionalOnBean( UserIdResolver.class )
   public UserContextBinder userContextBinder( UserIdResolver userIdResolver ) {
      return new UserContextBinder( userIdResolver );
   }
}
