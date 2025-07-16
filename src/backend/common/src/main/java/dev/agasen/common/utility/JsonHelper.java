package dev.agasen.common.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public record JsonHelper(
      ObjectMapper objectMapper
) {

   public String prettyPrint( String json ) {
      try {
         return objectMapper.writerWithDefaultPrettyPrinter()
               .writeValueAsString( objectMapper.readTree( json ) );
      } catch ( JsonProcessingException e ) {
         throw new RuntimeException( e );
      }
   }

}
