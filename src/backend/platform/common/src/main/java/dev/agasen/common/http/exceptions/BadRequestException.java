package dev.agasen.common.http.exceptions;

public class BadRequestException extends RuntimeException {

   public BadRequestException( String message ) {
      super( message );
   }
}
