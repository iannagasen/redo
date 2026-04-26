package dev.agasen.platform.core.context.user;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link UserContextBinder} correctly extracts the Bearer token from
 * the {@code Authorization} header and binds it into {@link UserContext#CURRENT_TOKEN}.
 *
 * <p>If token binding is ever removed from the binder, these tests catch it before
 * inter-service token forwarding silently breaks at runtime.
 */
class UserContextBinderTest {

   @Test
   void bindsToken_fromAuthorizationHeader() throws Exception {
      var binder = new UserContextBinder( req -> Optional.of( "user-1" ) );
      var request = new MockHttpServletRequest();
      request.addHeader( "Authorization", "Bearer test-token-abc" );

      AtomicReference< String > captured = new AtomicReference<>();
      binder.doFilter( request, new MockHttpServletResponse(), ( req, res ) ->
         captured.set( UserContext.isTokenBound() ? UserContext.currentToken() : null ) );

      assertThat( captured.get() ).isEqualTo( "test-token-abc" );
   }

   @Test
   void bindsUserId_fromResolver() throws Exception {
      var binder = new UserContextBinder( req -> Optional.of( "user-42" ) );

      AtomicReference< String > captured = new AtomicReference<>();
      binder.doFilter( new MockHttpServletRequest(), new MockHttpServletResponse(), ( req, res ) ->
         captured.set( UserContext.isBound() ? UserContext.currentUserId() : null ) );

      assertThat( captured.get() ).isEqualTo( "user-42" );
   }

   @Test
   void doesNotBindToken_whenNoAuthorizationHeader() throws Exception {
      var binder = new UserContextBinder( req -> Optional.of( "user-1" ) );

      AtomicBoolean tokenBound = new AtomicBoolean( true );
      binder.doFilter( new MockHttpServletRequest(), new MockHttpServletResponse(), ( req, res ) ->
         tokenBound.set( UserContext.isTokenBound() ) );

      assertThat( tokenBound.get() ).isFalse();
   }

   @Test
   void doesNotBindToken_whenAuthorizationIsNotBearer() throws Exception {
      var binder = new UserContextBinder( req -> Optional.of( "user-1" ) );
      var request = new MockHttpServletRequest();
      request.addHeader( "Authorization", "Basic dXNlcjpwYXNz" );

      AtomicBoolean tokenBound = new AtomicBoolean( true );
      binder.doFilter( request, new MockHttpServletResponse(), ( req, res ) ->
         tokenBound.set( UserContext.isTokenBound() ) );

      assertThat( tokenBound.get() ).isFalse();
   }

   @Test
   void skipsBindingEntirely_whenUserIdNotResolved() throws Exception {
      var binder = new UserContextBinder( req -> Optional.empty() );
      var request = new MockHttpServletRequest();
      request.addHeader( "Authorization", "Bearer some-token" );

      AtomicBoolean userBound = new AtomicBoolean( true );
      AtomicBoolean tokenBound = new AtomicBoolean( true );
      binder.doFilter( request, new MockHttpServletResponse(), ( req, res ) -> {
         userBound.set( UserContext.isBound() );
         tokenBound.set( UserContext.isTokenBound() );
      } );

      assertThat( userBound.get() ).isFalse();
      assertThat( tokenBound.get() ).isFalse();
   }
}
