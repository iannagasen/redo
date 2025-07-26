package dev.agasen.core.product.domain;

import java.math.BigDecimal;
import java.util.Map;

public record ProductCreationData(
      String name,
      String description,
      String sku,
      String slug,
      String brand,
      BigDecimal price,
      String currency,
      Integer stockQuantity,
      Boolean isActive,
      Boolean isFeatured,
      Map< String, Object > attributesJson,
      String category
) {

}