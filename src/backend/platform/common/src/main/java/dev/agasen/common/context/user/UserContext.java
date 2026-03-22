package dev.agasen.common.context.user;

/**
 * Holds the authenticated user's ID for the current operation using a {@link ScopedValue}.
 *
 * <p>{@link ScopedValue} is preferred over {@link ThreadLocal} because it is:
 * <ul>
 *   <li>Safe with virtual threads — no carrier thread pinning</li>
 *   <li>Immutable within a scope — cannot be accidentally overwritten mid-request</li>
 *   <li>Automatically cleaned up when the scope exits</li>
 *   <li>Inherited by {@code StructuredTaskScope} forks</li>
 * </ul>
 *
 * <p>For REST requests, {@link UserContextBinder} binds the value before the request reaches
 * any service. For Kafka consumers, bind explicitly before delegating to a service:
 * <pre>{@code
 * ScopedValue.where(UserContext.CURRENT_USER, event.userId())
 *     .run(() -> orderCommandService.handlePaymentResult(event));
 * }</pre>
 *
 * <p>Services read the value via {@link #currentUserId()} with no knowledge of who called them.
 */
public final class UserContext {

   /**
    * The scoped value holding the current user's ID.
    * Bound per-request by {@link UserContextBinder} for REST, or manually for other callers.
    */
   public static final ScopedValue< String > CURRENT_USER = ScopedValue.newInstance();

   /**
    * The scoped value holding the current request's raw Bearer token.
    */
   public static final ScopedValue< String > CURRENT_TOKEN = ScopedValue.newInstance();

   /**
    * Returns the current user's ID.
    *
    * @throws java.util.NoSuchElementException if called outside a bound scope
    */
   public static String currentUserId() {
      return CURRENT_USER.get();
   }

   /**
    * Returns the current Bearer token value.
    *
    * @throws java.util.NoSuchElementException if called outside a bound scope
    */
   public static String currentToken() {
      return CURRENT_TOKEN.get();
   }

   /**
    * Returns {@code true} if a user ID is bound in the current scope.
    */
   public static boolean isBound() {
      return CURRENT_USER.isBound();
   }

   /**
    * Returns {@code true} if a Bearer token is bound in the current scope.
    */
   public static boolean isTokenBound() {
      return CURRENT_TOKEN.isBound();
   }

   private UserContext() {
   }
}
