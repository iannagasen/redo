package dev.agasen.core.product.domain;

import dev.agasen.common.domain.mapper.EntityDomainMapper;
import dev.agasen.core.product.persistence.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper implements EntityDomainMapper<Product, ProductEntity> {

    public ProductEntity toEntity(Product domain) {
        return new ProductEntity(
                domain.id(),
                domain.name(),
                domain.description(),
                domain.sku(),
                domain.slug(),
                domain.brand(),
                domain.price(),
                domain.currency(),
                domain.stockQuantity(),
                domain.isActive(),
                domain.isFeatured(),
                domain.attributesJson(),
                domain.categoryId(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    public Product toDomain(ProductEntity entity) {
        return switch (entity) {
            case ProductEntity(
                    var id,
                    var name,
                    var description,
                    var sku,
                    var slug,
                    var brand,
                    var price,
                    var currency,
                    var stockQuantity,
                    var isActive,
                    var isFeatured,
                    var attributesJson,
                    var categoryId,
                    var createdAt,
                    var updatedAt
            ) -> new Product(
                    id,
                    name,
                    description,
                    sku,
                    slug,
                    brand,
                    price,
                    currency,
                    stockQuantity,
                    isActive,
                    isFeatured,
                    attributesJson,
                    categoryId,
                    createdAt,
                    updatedAt
            );
        };
    }
}