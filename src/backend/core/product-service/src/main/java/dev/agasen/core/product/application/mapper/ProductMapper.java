package dev.agasen.core.product.application.mapper;

import dev.agasen.api.core.product.product.ProductCreationDetails;
import dev.agasen.api.core.product.product.ProductDetails;
import dev.agasen.core.product.domain.product.Product;
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

   @Mapping( target = "imageUrl", ignore = true )
   ProductDetails toProductDetails( Product product );

   @Mapping( target = "imageKey", ignore = true )
   Product toEntity( ProductDetails productDetails );

   @Mapping( target = "cart", ignore = true )
   @Mapping( target = "bought", ignore = true )
   @Mapping( target = "imageKey", ignore = true )
   Product toEntity( ProductCreationDetails productCreationDetails );

   @Mapping( target = "imageUrl", ignore = true )
   ProductDetails toDomain( Product product );

}
