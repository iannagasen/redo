package dev.agasen.api;

import dev.agasen.common.exceptions.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Order( Ordered.LOWEST_PRECEDENCE )
@ControllerAdvice
public class GlobalExceptionHandling {

   @ExceptionHandler( NotFoundException.class )
   public ResponseEntity< Map< String, Object > > handleNotFound( NotFoundException ex ) {
      var body = new HashMap< String, Object >();
      body.put( "timestamp", LocalDateTime.now() );
      body.put( "status", HttpStatus.NOT_FOUND.value() );
      body.put( "error", "Not Found" );
      body.put( "message", ex.getMessage() );
      return new ResponseEntity<>( body, HttpStatus.NOT_FOUND );
   }

}
