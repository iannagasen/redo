package dev.agasen.core.product.domain;

import java.math.BigDecimal;
import java.util.Map;

public record ProductCreationDetails(
      String name,
      String description,
      String sku,
      String slug,
      String brand,
      BigDecimal price,
      String currency,
      int stock,
      Map< String, Object > attributesJson
) {
}