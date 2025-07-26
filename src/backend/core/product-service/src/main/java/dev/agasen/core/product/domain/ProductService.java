package dev.agasen.core.product.domain;

import dev.agasen.common.cache.CachingService;
import dev.agasen.core.product.persistence.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Supplier;

@Service
public record ProductService(
      ProductRepository productRepository,
      ProductMapper productMapper,
      CachingService< String, Product > productCachingService
) {

   public Page< Product > getProducts( int page, int size ) {
      return productRepository.findAll( PageRequest.of( page, size ) )
            .map( productMapper::toDomain );
   }

   public Page< Product > getProductsByCategory( String category, int page, int size ) {
      return productRepository.findAllByCategoryId( UUID.fromString( category ), PageRequest.of( page, size ) )
            .map( productMapper::toDomain );
   }

   public Product getProduct( String id ) {
      Supplier< Product > findByProductInDb = () -> productRepository.findById( UUID.fromString( id ) )
            .map( productMapper::toDomain )
            .orElseThrow( () -> new RuntimeException( "Product not found with id: " + id ) );

      return productCachingService.getCachedOrCompute( id, findByProductInDb );
   }

   public Product createProduct( ProductCreationData productCreationData ) {
      var product = Product.create( productCreationData );
      var entity = productMapper.toEntity( product );
      var saved = productRepository.save( entity );
      return productMapper.toDomain( saved );
   }

}