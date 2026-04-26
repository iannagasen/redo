package dev.agasen.platform.contracts.core.event;

import java.math.BigDecimal;

public record PaymentEvent(
   Long orderId,
   Long paymentId,
   String userId,
   BigDecimal amount,
   String status,
   String failureReason
) {
}
