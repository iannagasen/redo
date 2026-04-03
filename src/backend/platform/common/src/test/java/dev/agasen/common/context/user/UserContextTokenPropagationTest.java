package dev.agasen.common.context.user;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

   // ── Not-null ───────────────────────────────────────────────────────────────

   @Test
   void currentUserId_isNotNull_whenBound() throws Exception {
      ScopedValue.where( UserContext.CURRENT_USER, "user-1" ).call( () -> {
         assertThat( UserContext.currentUserId() ).isNotNull().isEqualTo( "user-1" );
         return null;
      } );
   }

   @Test
   void isBound_returnsFalse_whenCalledOutsideScope() {
      assertThat( UserContext.isBound() ).isFalse();
   }

   // ── Isolation: each virtual thread sees only its own value ─────────────────

   /**
    * 10 000 concurrent virtual threads each bind a unique userId.
    * Every thread must read back exactly its own value — never another thread's.
    * <p>
    * This is the core correctness property for high concurrency:
    * ScopedValue stores its value on each thread's own call-stack frame, so
    * there is NO shared state between threads. This would silently break with
    * a ThreadLocal if threads were reused without cleanup.
    */
   @Test
   void currentUserId_isIsolated_acrossConcurrentVirtualThreads() throws Exception {
      int threadCount = 10_000;
      var violations = new ArrayList< String >();

      try ( var scope = StructuredTaskScope.open( Joiner.allSuccessfulOrThrow() ) ) {
         for ( int i = 0; i < threadCount; i++ ) {
            final String expectedId = "user-" + i;
            scope.fork( () -> {
               ScopedValue.where( UserContext.CURRENT_USER, expectedId ).call( () -> {
                  String actual = UserContext.currentUserId();
                  if ( !expectedId.equals( actual ) ) {
                     synchronized ( violations ) {
                        violations.add( "expected=" + expectedId + " actual=" + actual );
                     }
                  }
                  return null;
               } );
               return null;
            } );
         }
         scope.join();
      }

      assertThat( violations )
         .as( "Scope isolation violations (cross-thread contamination)" )
         .isEmpty();
   }
}
