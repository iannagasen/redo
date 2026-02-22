package dev.agasen.api.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddToCartRequest {
   @NotNull
   private Long productId;
   @NotBlank
   private String productName;
   private String brand;
   @NotNull
   private BigDecimal price;
   private String currency;
   @Min( 1 )
   private int quantity = 1;
}
