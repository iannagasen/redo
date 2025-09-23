package dev.agasen.core.product.commands;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.core.product.mapper.ProductMapper;
import dev.agasen.core.product.persistence.ProductRepository;
import dev.agasen.core.product.persistence.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CreateProductService {
   private final ProductRepository productRepository;
   private final ProductMapper productMapper;


   @PreAuthorize( """
          hasAuthority('SCOPE_product:write-create') or
          hasAuthority('SCOPE_openid')
      """ )
   public ProductDetails create( ProductCreationDetails productCreationDetails ) {
      Product mapped = productMapper.toEntity( productCreationDetails );
      Product saved = productRepository.save( mapped );
      return productMapper.toProductDetails( saved );
   }
}
