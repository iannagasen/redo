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

    public static Product create(CreateProductDTO dto) {
        return new Product(
                UUID.randomUUID(),
                dto.name(),
                dto.description(),
                dto.sku(),
                dto.slug(),
                dto.brand(),
                dto.price(),
                dto.currency(),
                dto.stockQuantity(),
                dto.isActive(),
                dto.isFeatured(),
                dto.attributesJson(),
                UUID.fromString(dto.category()),
                Instant.now(),
                Instant.now()
        );
    }

}