package dev.agasen.common.validations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {
   private final List< String > errors = new ArrayList<>();

   public static ValidationResult empty() {
      return new ValidationResult();
   }

   public static ValidationResult errors( List< String > errors ) {
      ValidationResult result = new ValidationResult();
      result.errors.addAll( errors );
      return result;
   }

   public void addError( String message ) {
      errors.add( message );
   }

   public boolean isValid() {
      return errors.isEmpty();
   }

   public List< String > getErrors() {
      return Collections.unmodifiableList( errors );
   }
}
