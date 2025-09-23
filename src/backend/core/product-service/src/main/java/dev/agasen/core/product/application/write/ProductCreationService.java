package dev.agasen.core.product.application.write;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.core.product.application.mapper.ProductMapper;
import dev.agasen.core.product.domain.product.Product;
import dev.agasen.core.product.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductCreationService {

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
