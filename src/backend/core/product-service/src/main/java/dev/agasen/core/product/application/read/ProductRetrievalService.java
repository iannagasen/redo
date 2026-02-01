package dev.agasen.core.product.application.read;

import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.common.cache.CachingService;
import dev.agasen.common.pagination.PagedResult;
import dev.agasen.core.product.application.mapper.ProductMapper;
import dev.agasen.core.product.domain.product.ProductRepository;
import dev.agasen.core.product.infrastructure.cache.PageProductDetailsCachingServiceConfig;
import lombok.RequiredArgsConstructor;
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
   private final CachingService< String, PagedResult< ProductDetails > > productDetailsCacheService;

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public List< ProductDetails > getAllProducts() {
      return null;
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public PagedResult< ProductDetails > getAllProducts( int page, int size ) {
      String key = PageProductDetailsCachingServiceConfig.KEY_PREFIX + "p-" + page + ":s-" + size;
      return productDetailsCacheService.getCachedOrCompute( key, () -> findAllProducts( page, size ) );
   }

   private PagedResult< ProductDetails > findAllProducts( int page, int size ) {
      return PagedResult.from(
         productRepository.findAll( PageRequest.of( page, size ) ).map( productMapper::toDomain )
      );
   }
}
