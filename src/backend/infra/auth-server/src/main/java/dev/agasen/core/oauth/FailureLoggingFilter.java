package dev.agasen.core.oauth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@Order( Ordered.HIGHEST_PRECEDENCE )
@Slf4j
public class FailureLoggingFilter extends OncePerRequestFilter {

   @Override
   protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain ) throws ServletException, IOException {

      try {
         filterChain.doFilter( request, response );

         if ( response.getStatus() >= 400 ) {
            log.warn( "Failed Response {} {}", response.getStatus(), request.getRequestURI() );
         }

      } catch ( Exception e ) {

         String paramsCsv = Collections.list( request.getParameterNames() )
            .stream()
            .map( name -> mapAndMaskSensitiveParameter( request, name ) )
            .collect( Collectors.joining( ", " ) );

         logError( request, e, paramsCsv );

         throw e;
      }

   }

   private void logError( HttpServletRequest request, Exception e, String paramesCsv ) {
      log.error( """
            FAILED REQUEST
            Method   : {}
            URI      : {}
            Query    : {}
            Params   : {}
            Error    : {}
            Message  : {}
            """,
         request.getMethod(),
         request.getRequestURI(),
         request.getQueryString(),
         paramesCsv,
         e.getClass().getName(),
         e.getMessage(),
         e
      );
   }

   private String mapAndMaskSensitiveParameter( HttpServletRequest req, String name ) {
      if ( name.toLowerCase().contains( "secret" ) || name.toLowerCase().contains( "password" ) ) {
         return name + "=***";
      } else {
         return name + "=" + req.getParameter( name );
      }
   }
}
