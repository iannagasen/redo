package dev.agasen.core.product.application.read;

import dev.agasen.platform.contracts.core.product.product.ProductDetails;
import dev.agasen.platform.core.cache.CachingService;
import dev.agasen.platform.core.http.pagination.PagedResult;
import dev.agasen.core.product.application.mapper.ProductMapper;
import dev.agasen.core.product.domain.product.Product;
import dev.agasen.core.product.domain.product.ProductRepository;
import dev.agasen.core.product.infrastructure.cache.PageProductDetailsCachingServiceConfig;
import dev.agasen.core.product.infrastructure.s3.ProductImageBucket;
import dev.agasen.platform.core.storage.FileReference;
import dev.agasen.platform.core.storage.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RequiredArgsConstructor
@Transactional( readOnly = true )
@Service
public class ProductRetrievalService {

   private final ProductRepository productRepository;
   private final ProductMapper productMapper;
   private final FileStoragePort< ProductImageBucket > productImageFileStoragePort;
   private final CachingService< String, PagedResult< ProductDetails > > productDetailsCacheService;
   private final CachingService< String, ProductDetails > productCachingService;

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public List< ProductDetails > getAllProducts() {
      return null;
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public PagedResult< ProductDetails > getAllProducts( int page, int size ) {
      String key = PageProductDetailsCachingServiceConfig.KEY_PREFIX + "p-" + page + ":s-" + size;
      return productDetailsCacheService.getCachedOrCompute( key, () -> findAllProducts( page, size ) );
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public List< ProductDetails > getProductsByIds( List< Long > ids ) {
      return productRepository.findAllByIdIn( ids )
         .stream()
         .map( this::toDetailsWithImageUrl )
         .toList();
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public ProductDetails getProductById( Long id ) {
      return productCachingService.getCachedOrCompute( String.valueOf( id ), () -> findProductById( id ) );
   }

   private PagedResult< ProductDetails > findAllProducts( int page, int size ) {
      return PagedResult.from(
         productRepository.findAll( PageRequest.of( page, size ) ).map( this::toDetailsWithImageUrl )
      );
   }

   private ProductDetails findProductById( Long id ) {
      return productRepository.findById( id )
         .map( this::toDetailsWithImageUrl )
         .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND, "Product not found: " + id ) );
   }

   private ProductDetails toDetailsWithImageUrl( Product product ) {
      ProductDetails details = productMapper.toDomain( product );
      if ( product.getImageKey() != null ) {
         details.setImageUrl( productImageFileStoragePort.getAccessUri( new FileReference( product.getImageKey() ) ).toString() );
      }
      return details;
   }
}
