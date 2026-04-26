package dev.agasen.platform.core.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonHelper {

   private final ObjectMapper objectMapper = new ObjectMapper();

   public String prettyPrint( String json ) {
      try {
         return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString( objectMapper.readTree( json ) );
      } catch ( JsonProcessingException e ) {
         throw new RuntimeException( e );
      }
   }

}
