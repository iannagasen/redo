package dev.agasen.common.validations;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.List;

public final class BeanValidator {
   private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

   public ValidationResult validate( Object bean ) {
      var violations = VALIDATOR.validate( bean );

      if ( violations.isEmpty() ) {
         return ValidationResult.empty();
      }

      List< String > errors = violations.stream()
            .map( v -> v.getPropertyPath() + " " + v.getMessage() )
            .toList();

      return ValidationResult.errors( errors );
   }
}
