package dev.agasen.common.exceptions;

import java.util.function.Supplier;

public class Exceptions {

   public static Supplier< NotFoundException > notFound( String entity, Object id ) {
      return () -> new NotFoundException( "Entity not found: " + entity + " with id: " + id );
   }

   public static Supplier< NotFoundException > notFound( String message ) {
      return () -> new NotFoundException( message );
   }

}
