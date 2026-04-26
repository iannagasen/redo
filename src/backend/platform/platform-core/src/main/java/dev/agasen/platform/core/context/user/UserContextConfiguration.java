package dev.agasen.platform.core.context.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Abstract base configuration that enforces {@link UserIdResolver} presence at compile time.
 *
 * <p>Each service that needs {@link UserContext} populated per-request must extend this class
 * and implement {@link #userIdResolver()}. Forgetting the implementation is a compile error.
 *
 * <p>The {@link UserContextBinder} filter bean is provided automatically by
 * {@link UserContextAutoConfiguration} and requires the {@link UserIdResolver} bean produced
 * here. If a service has no {@link UserIdResolver} in its context, startup fails with an
 * unsatisfied dependency error.
 *
 * <p>Example:
 * <pre>{@code
 * @Configuration
 * public class SecurityConfig extends UserContextConfiguration {
 *
 *     @Bean @Override
 *     public UserIdResolver userIdResolver() {
 *         return request -> Optional.ofNullable(
 *             SecurityContextHolder.getContext().getAuthentication()
 *         ).filter(Authentication::isAuthenticated).map(Authentication::getName);
 *     }
 * }
 * }</pre>
 */
@Configuration
public abstract class UserContextConfiguration {

   /**
    * Resolves the current user's ID from an incoming HTTP request.
    *
    * <p>Subclasses must implement this method. The compiler refuses to compile a class that
    * extends {@link UserContextConfiguration} without providing this implementation.
    */
   @Bean
   public abstract UserIdResolver userIdResolver();
}
