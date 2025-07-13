package dev.agasen.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Table("products")
public record ProductEntity(
        @Id UUID id,
        String name,
        String description,
        String sku,
        String slug,
        String brand,
        BigDecimal price,
        String currency,
        @Column("stock_quantity") Integer stockQuantity,
        @Column("is_active") Boolean isActive,
        @Column("is_featured") Boolean isFeatured,
        @Column("attributes") Map<String, Object> attributesJson,
        @Column("category_id") UUID categoryId,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt
) {

}
