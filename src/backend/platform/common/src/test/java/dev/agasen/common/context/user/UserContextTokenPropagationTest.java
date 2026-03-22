package dev.agasen.common.context.user;

import org.junit.jupiter.api.Test;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link UserContext} ScopedValues are inherited by threads spawned via
 * {@link StructuredTaskScope#fork} — the property that makes token forwarding work
 * without {@code ThreadLocal} propagation hacks.
 *
 * <p>These tests would FAIL if {@code CURRENT_TOKEN} or {@code CURRENT_USER} were
 * changed to a {@code ThreadLocal}, catching that regression immediately.
 */
class UserContextTokenPropagationTest {

   @Test
   void currentToken_isInherited_byStructuredTaskScopeForkedThread() throws Exception {
      String token = "test-bearer-token";
      AtomicReference< String > captured = new AtomicReference<>();

      ScopedValue.where( UserContext.CURRENT_TOKEN, token ).call( () -> {
         try ( var scope = StructuredTaskScope.open( Joiner.allSuccessfulOrThrow() ) ) {
            var task = scope.fork( () -> UserContext.currentToken() );
            scope.join();
            captured.set( task.get() );
         }
         return null;
      } );

      assertThat( captured.get() ).isEqualTo( token );
   }

   @Test
   void currentUserId_isInherited_byStructuredTaskScopeForkedThread() throws Exception {
      String userId = "user-123";
      AtomicReference< String > captured = new AtomicReference<>();

      ScopedValue.where( UserContext.CURRENT_USER, userId ).call( () -> {
         try ( var scope = StructuredTaskScope.open( Joiner.allSuccessfulOrThrow() ) ) {
            var task = scope.fork( () -> UserContext.currentUserId() );
            scope.join();
            captured.set( task.get() );
         }
         return null;
      } );

      assertThat( captured.get() ).isEqualTo( userId );
   }

   @Test
   void bothValues_areInherited_whenBoundTogether() throws Exception {
      AtomicReference< String > capturedToken = new AtomicReference<>();
      AtomicReference< String > capturedUser = new AtomicReference<>();

      ScopedValue.where( UserContext.CURRENT_USER, "user-42" )
         .where( UserContext.CURRENT_TOKEN, "token-xyz" )
         .call( () -> {
            try ( var scope = StructuredTaskScope.open( Joiner.allSuccessfulOrThrow() ) ) {
               var tokenTask = scope.fork( () -> UserContext.currentToken() );
               var userTask = scope.fork( () -> UserContext.currentUserId() );
               scope.join();
               capturedToken.set( tokenTask.get() );
               capturedUser.set( userTask.get() );
            }
            return null;
         } );

      assertThat( capturedToken.get() ).isEqualTo( "token-xyz" );
      assertThat( capturedUser.get() ).isEqualTo( "user-42" );
   }
}
