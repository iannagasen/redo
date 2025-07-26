package dev.agasen.core.product.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter( autoApply = true )
public class MapToJsonbConverter implements AttributeConverter< Map< String, Object >, String > {

   @Override
   public String convertToDatabaseColumn( Map< String, Object > attribute ) {
      try {
         return new ObjectMapper().writeValueAsString( attribute );
      } catch ( JsonProcessingException e ) {
         throw new IllegalArgumentException( "Unable to convert map to JSON", e );
      }
   }

   @Override
   public Map< String, Object > convertToEntityAttribute( String dbData ) {
      try {
         return new ObjectMapper().readValue( dbData, new TypeReference< Map< String, Object > >() {
         } );
      } catch ( IOException e ) {
         throw new IllegalArgumentException( "Unable to convert JSON to map", e );
      }
   }
}
