package dev.agasen.api.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentDetails {
   private Long id;
   private Long orderId;
   private String userId;
   private BigDecimal amount;
   private String currency;
   private String status;
   private Instant createdAt;
}
