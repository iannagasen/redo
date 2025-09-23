package dev.agasen.core.product.application.read;

import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.core.product.application.mapper.ProductMapper;
import dev.agasen.core.product.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional( readOnly = true )
@Service
public class ProductRetrievalService {

   private final ProductRepository productRepository;
   private final ProductMapper productMapper;
   // include caching

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public List< ProductDetails > getAllProducts() {
      return null;
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public Page< ProductDetails > getAllProducts( int page, int size ) {
      return productRepository.findAll( PageRequest.of( page, size ) )
         .map( productMapper::toDomain );
   }

}
