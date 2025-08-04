package dev.agasen.api.common;

public record Updated< ENTITY, ID >(
      ID id,
      ENTITY entity,
      String message
) {
   private static final String DEFAULT_MESSAGE = "Successfully updated";

   public static < ENTITY, ID > Updated< ENTITY, ID > create() {
      return new Updated< ENTITY, ID >( null, null, DEFAULT_MESSAGE );
   }

   public static < ENTITY, ID > Updated< ENTITY, ID > withOnlyMessage( String message ) {
      return new Updated< ENTITY, ID >( null, null, message );
   }

   public static < ENTITY, ID > Updated< ENTITY, ID > withId( ID id ) {
      return new Updated<>( id, null, DEFAULT_MESSAGE );
   }

   public static < ENTITY, ID > Updated< ENTITY, ID > withEntity( ID id, ENTITY entity ) {
      return new Updated<>( id, entity, DEFAULT_MESSAGE );
   }

   public static < ENTITY, ID > Updated< ENTITY, ID > withCustomMessage( ID id, ENTITY entity, String message ) {
      return new Updated<>( id, entity, message );
   }
}

