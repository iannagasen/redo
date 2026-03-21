package dev.agasen.api.order;

import dev.agasen.api.product.product.ProductDetails;

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
