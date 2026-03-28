package dev.agasen.api.core.order.read;

import dev.agasen.api.core.payment.read.PaymentDetails;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderSummary(
   Long id,
   String userId,
   String status,
   BigDecimal total,
   int itemCount,
   Instant createdAt,
   List< OrderSummaryItem > items,
   PaymentDetails payment
) {

   public OrderSummary( OrderDetails orderDetails, PaymentDetails paymentDetails, List< OrderSummaryItem > items ) {
      this(
         orderDetails.getId(),
         orderDetails.getUserId(),
         orderDetails.getStatus(),
         orderDetails.getTotal(),
         orderDetails.getItemCount(),
         orderDetails.getCreatedAt(),
         items,
         paymentDetails
      );
   }

}
