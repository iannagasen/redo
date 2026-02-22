package dev.agasen.api.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDetails {
   private Long productId;
   private String productName;
   private String brand;
   private BigDecimal price;
   private String currency;
   private int quantity;
   private BigDecimal lineTotal;
}
