package dev.agasen.core.product.domain;

import dev.agasen.common.domain.mapper.EntityDomainMapper;
import dev.agasen.core.product.persistence.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper implements EntityDomainMapper< Product, ProductEntity > {

   @Override
   public ProductEntity toEntity( Product domain ) {
      ProductEntity entity = new ProductEntity();
      entity.setId( domain.id() );
      entity.setName( domain.name() );
      entity.setDescription( domain.description() );
      entity.setSku( domain.sku() );
      entity.setSlug( domain.slug() );
      entity.setBrand( domain.brand() );
      entity.setPrice( domain.price() );
      entity.setCurrency( domain.currency() );
      entity.setStockQuantity( domain.stockQuantity() );
      entity.setAttributesJson( domain.attributesJson() );
      entity.setCategoryId( domain.categoryId() );
      entity.setCreatedAt( domain.createdAt() );
      entity.setUpdatedAt( domain.updatedAt() );
      return entity;
   }

   @Override
   public Product toDomain( ProductEntity entity ) {
      return new Product(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSku(),
            entity.getSlug(),
            entity.getBrand(),
            entity.getPrice(),
            entity.getCurrency(),
            entity.getStockQuantity(),
            entity.getAttributesJson(),
            entity.getCategoryId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
      );
   }
}
