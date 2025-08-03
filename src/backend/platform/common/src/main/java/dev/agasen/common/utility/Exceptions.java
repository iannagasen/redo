package dev.agasen.common.utility;

import java.util.function.Supplier;

public class Exceptions {

   public static Supplier< RuntimeException > notFound( String entity, Object id ) {
      return () -> new RuntimeException( "Entity not found: " + entity + " with id: " + id );
   }

}
