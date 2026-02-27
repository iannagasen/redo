package dev.agasen.api.event;

import java.math.BigDecimal;

public record PaymentEvent(
      Long orderId,
      Long paymentId,
      String userId,
      BigDecimal amount,
      String status,
      String failureReason
) {}
