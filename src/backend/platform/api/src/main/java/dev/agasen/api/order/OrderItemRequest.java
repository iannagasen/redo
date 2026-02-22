package dev.agasen.api.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {
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
