package dev.agasen.api.core.order.read;

import java.math.BigDecimal;

public record OrderSummaryItem(
   Long productId,
   String productName,
   String brand,
   String description,
   BigDecimal price,
   String currency,
   int quantity,
   BigDecimal lineTotal
) {

}
