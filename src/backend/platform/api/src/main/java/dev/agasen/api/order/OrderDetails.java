package dev.agasen.api.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderDetails {
   private Long id;
   private String userId;
   private String status;
   private BigDecimal total;
   private int itemCount;
   private List<OrderItemDetails> items;
   private Instant createdAt;
}
