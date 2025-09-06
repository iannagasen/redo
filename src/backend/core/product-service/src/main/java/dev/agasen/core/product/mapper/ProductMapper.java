package dev.agasen.core.product.mapper;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.core.product.persistence.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
      componentModel = "spring",
      unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProductMapper {

   ProductMapper INSTANCE = Mappers.getMapper( ProductMapper.class );

   ProductDetails toProductDetails( Product product );

   @Mapping( target = "productModel", ignore = true )
   @Mapping( target = "attributes", ignore = true )
   Product toEntity( ProductDetails productDetails );

   @Mapping( target = "productModel", ignore = true )
   @Mapping( target = "id", ignore = true )
   @Mapping( target = "cart", ignore = true )
   @Mapping( target = "bought", ignore = true )
   @Mapping( target = "attributes", ignore = true )
   Product toEntity( ProductCreationDetails productCreationDetails );

}

/**
 * public ProductDetails toDomain( Product entity ) {
 * return new ProductDetails(
 * entity.getId(),
 * entity.getName(),
 * entity.getDescription(),
 * entity.getSku(),
 * entity.getSlug(),
 * entity.getBrand(),
 * entity.getPrice(),
 * entity.getCurrency(),
 * entity.getStock(),
 * entity.getBought(),
 * entity.getCart()
 * //            entity.getAttributesJson()
 * );
 * }
 * <p>
 * public Product toEntity( ProductCreationDetails creationData ) {
 * Product entity = new Product();
 * entity.setName( creationData.name() );
 * entity.setDescription( creationData.description() );
 * entity.setSku( creationData.sku() );
 * entity.setSlug( creationData.slug() );
 * entity.setBrand( creationData.brand() );
 * entity.setPrice( creationData.price() );
 * entity.setCurrency( creationData.currency() );
 * entity.setStock( creationData.stock() );
 * //      entity.setAttributesJson( creationData.attributesJson() );
 * return entity;
 * }
 */