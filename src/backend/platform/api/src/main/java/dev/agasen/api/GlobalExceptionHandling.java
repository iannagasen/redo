package dev.agasen.api;

import dev.agasen.common.exceptions.BadRequestException;
import dev.agasen.common.exceptions.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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

   @ExceptionHandler( MethodArgumentNotValidException.class )
   public ResponseEntity< Map< String, Object > > handleMethodArgumentNotValid( MethodArgumentNotValidException ex ) {
      List< String > errors = ex.getBindingResult().getFieldErrors().stream()
         .map( fe -> fe.getField() + ": " + fe.getDefaultMessage() )
         .toList();
      var body = new HashMap< String, Object >();
      body.put( "timestamp", LocalDateTime.now() );
      body.put( "status", HttpStatus.BAD_REQUEST.value() );
      body.put( "error", "Bad Request" );
      body.put( "errors", errors );
      return new ResponseEntity<>( body, HttpStatus.BAD_REQUEST );
   }

   @ExceptionHandler( ConstraintViolationException.class )
   public ResponseEntity< Map< String, Object > > handleConstraintViolation( ConstraintViolationException ex ) {
      List< String > errors = ex.getConstraintViolations().stream()
         .map( cv -> cv.getPropertyPath() + ": " + cv.getMessage() )
         .toList();
      var body = new HashMap< String, Object >();
      body.put( "timestamp", LocalDateTime.now() );
      body.put( "status", HttpStatus.BAD_REQUEST.value() );
      body.put( "error", "Bad Request" );
      body.put( "errors", errors );
      return new ResponseEntity<>( body, HttpStatus.BAD_REQUEST );
   }

   @ExceptionHandler( BadRequestException.class )
   public ResponseEntity< Map< String, Object > > handleBadRequest( BadRequestException ex ) {
      var body = new HashMap< String, Object >();
      body.put( "timestamp", LocalDateTime.now() );
      body.put( "status", HttpStatus.BAD_REQUEST.value() );
      body.put( "error", "Bad Request" );
      body.put( "message", ex.getMessage() );
      return new ResponseEntity<>( body, HttpStatus.BAD_REQUEST );
   }
}
