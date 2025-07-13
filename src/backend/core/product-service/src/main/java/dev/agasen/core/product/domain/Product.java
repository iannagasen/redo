package dev.agasen.core.product.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Product(
        UUID id,
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
        Map<String, Object> attributesJson,
        UUID categoryId,
        Instant createdAt,
        Instant updatedAt
) {

}