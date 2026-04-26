package dev.agasen.platform.core.http.exceptions;

public class BadRequestException extends RuntimeException {

   public BadRequestException( String message ) {
      super( message );
   }
}
