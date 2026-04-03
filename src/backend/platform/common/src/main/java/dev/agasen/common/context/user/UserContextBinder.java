package dev.agasen.common.context.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that binds the current user's ID into {@link UserContext} for the duration
 * of each HTTP request.
 *
 * <p>Uses a {@link ScopedValue} scope wrapping the entire filter chain, so the user ID is
 * available to any code downstream — services, repositories, mappers — without passing it
 * as a method parameter.
 *
 * <p>This filter is automatically registered as a Spring bean. It requires a {@link UserIdResolver}
 * bean to be present in the application context. If none is provided, the application will
 * fail at startup. Each service is responsible for defining its own {@link UserIdResolver}.
 *
 * <p>For non-HTTP callers (e.g., Kafka consumers), bind the user ID manually:
 * <pre>{@code
 * ScopedValue.where(UserContext.CURRENT_USER, event.userId())
 *     .run(() -> someService.process(event));
 * }</pre>
 */
public class UserContextBinder extends OncePerRequestFilter {

   private final UserIdResolver userIdResolver;

   public UserContextBinder( UserIdResolver userIdResolver ) {
      this.userIdResolver = userIdResolver;
   }

   @Override
   protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
      throws ServletException, IOException {

      var userId = userIdResolver.resolve( request );

      if ( userId.isEmpty() ) {
         chain.doFilter( request, response );
         return;
      }

      var token = extractBearerToken( request );
      var scope = ScopedValue.where( UserContext.CURRENT_USER, userId.get() );

      try {
         if ( token.isPresent() ) {
            scope.where( UserContext.CURRENT_TOKEN, token.get() )
               .call( () -> {
                  chain.doFilter( request, response );
                  return null;
               } );
         } else {
            scope.call( () -> {
               chain.doFilter( request, response );
               return null;
            } );
         }
      } catch ( IOException | ServletException e ) {
         throw e;
      } catch ( Exception e ) {
         throw new ServletException( e );
      }
   }

   private java.util.Optional<String> extractBearerToken( HttpServletRequest request ) {
      var header = request.getHeader( "Authorization" );
      if ( header != null && header.startsWith( "Bearer " ) ) {
         return java.util.Optional.of( header.substring( 7 ) );
      }
      return java.util.Optional.empty();
   }
}
