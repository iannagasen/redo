package dev.agasen.api.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDetails {
   private String userId;
   private List<CartItemDetails> items;
   private BigDecimal total;
   private int itemCount;
}
