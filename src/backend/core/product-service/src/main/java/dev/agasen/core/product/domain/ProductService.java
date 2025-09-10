package dev.agasen.core.product.domain;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.common.cache.CachingService;
import dev.agasen.core.product.mapper.ProductMapper;
import dev.agasen.core.product.persistence.ProductRepository;
import dev.agasen.core.product.persistence.entity.Product;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class ProductService {

   private final ProductRepository productRepository;
   private final ProductMapper productMapper;
   private final CachingService< String, ProductDetails > productCachingService;

   @Transactional
   public List< Product > getAllProducts() {
      var products = productRepository.findAll();
//      var productModel = products.get( 0 ).getProductModel();
      return products;
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_read', 'SCOPE_openid')" )
   public Page< ProductDetails > getProducts( int page, int size ) {
      return productRepository.findAll( PageRequest.of( page, size ) )
            .map( productMapper::toDomain );
   }

   public Page< ProductDetails > getProductsByCategory( String category, int page, int size ) {
//      return productRepository.findAllByCategoryId( UUID.fromString( category ), PageRequest.of( page, size ) )
//            .map( productMapper::toDomain );
      return null;
   }

   public ProductDetails getProduct( Long id ) {
      Supplier< ProductDetails > findByProductInDb = () -> productRepository.findById( id )
            .map( productMapper::toDomain )
            .orElseThrow( () -> new RuntimeException( "Product not found with id: " + id ) );

      return productCachingService.getCachedOrCompute( id.toString(), findByProductInDb );
   }

   public ProductDetails createProduct( ProductCreationDetails productCreationDetails ) {
      var entity = productMapper.toEntity( productCreationDetails );
      var saved = productRepository.save( entity );
      return productMapper.toDomain( saved );
   }

}