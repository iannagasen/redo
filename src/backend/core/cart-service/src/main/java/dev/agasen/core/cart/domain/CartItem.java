package dev.agasen.core.cart.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {
   private Long productId;
   private String productName;
   private String brand;
   private BigDecimal price;
   private String currency;
   private int quantity;
}
