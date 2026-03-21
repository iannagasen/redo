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
   public static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();

   /**
    * Returns the current user's ID.
    *
    * @throws java.util.NoSuchElementException if called outside a bound scope
    */
   public static String currentUserId() {
      return CURRENT_USER.get();
   }

   /**
    * Returns {@code true} if a user ID is bound in the current scope.
    * Useful in contexts where the caller may be unauthenticated (e.g., public endpoints).
    */
   public static boolean isBound() {
      return CURRENT_USER.isBound();
   }

   private UserContext() {}
}
