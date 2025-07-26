package dev.agasen.core.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDetails(
      UUID id,
      String name,
      String description,
      String sku,
      String slug,
      String brand,
      BigDecimal price,
      String currency,
      int stock,
      int bought,
      int cart
//      Map< String, Object > attributesJson
) {

}
