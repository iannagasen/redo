package dev.agasen.api.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitiatePaymentRequest {

   @NotNull
   private Long orderId;

   @NotNull
   @Positive
   private BigDecimal amount;

   @NotBlank
   private String currency;

   @NotBlank
   private String cardNumber;

   @NotBlank
   private String cardholderName;

   @NotNull
   private Integer expiryMonth;

   @NotNull
   private Integer expiryYear;

   @NotBlank
   private String cvv;
}
