package dev.agasen.platform.core.context.user;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Strategy for resolving the current user's ID from an incoming HTTP request.
 *
 * <p>Implementations are caller-specific and live in each service:
 * <ul>
 *   <li>REST services typically extract the user from the JWT via {@code SecurityContextHolder}</li>
 *   <li>Services with other auth mechanisms (API key, session) provide their own implementation</li>
 * </ul>
 *
 * <p>A bean of this type is required by {@link UserContextBinder}. If none is provided,
 * the application will fail at startup with an unsatisfied dependency error.
 *
 * <p>Example for a JWT-based service:
 * <pre>{@code
 * @Bean
 * public UserIdResolver userIdResolver() {
 *     return request -> {
 *         var auth = SecurityContextHolder.getContext().getAuthentication();
 *         if (auth == null || !auth.isAuthenticated()) return Optional.empty();
 *         return Optional.of(auth.getName());
 *     };
 * }
 * }</pre>
 */
@FunctionalInterface
public interface UserIdResolver {

   /**
    * Resolves the user ID from the given request.
    *
    * @param request the current HTTP request
    * @return the user ID, or {@code Optional.empty()} if the request is unauthenticated
    */
   Optional< String > resolve( HttpServletRequest request );
}
