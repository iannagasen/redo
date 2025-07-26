package dev.agasen.core.product.domain;

import dev.agasen.core.product.persistence.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

   public ProductDetails toDomain( Product entity ) {
      return new ProductDetails(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSku(),
            entity.getSlug(),
            entity.getBrand(),
            entity.getPrice(),
            entity.getCurrency(),
            entity.getStock(),
            entity.getBought(),
            entity.getCart()
//            entity.getAttributesJson()
      );
   }

   public Product toEntity( ProductCreationDetails creationData ) {
      Product entity = new Product();
      entity.setName( creationData.name() );
      entity.setDescription( creationData.description() );
      entity.setSku( creationData.sku() );
      entity.setSlug( creationData.slug() );
      entity.setBrand( creationData.brand() );
      entity.setPrice( creationData.price() );
      entity.setCurrency( creationData.currency() );
      entity.setStock( creationData.stock() );
//      entity.setAttributesJson( creationData.attributesJson() );
      return entity;
   }


}
