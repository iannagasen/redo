package dev.agasen.api.cart;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDetails {
   private Long productId;
   private String productName;
   private String brand;
   private BigDecimal price;
   private String currency;
   private int quantity;
   private BigDecimal lineTotal;
}
