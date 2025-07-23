package dev.agasen.core.product.domain;

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
      ProductCacheService productCacheService
) {
   public Page< Product > getProducts( int page, int size ) {
      return productRepository.findAll( PageRequest.of( page, size ) )
            .map( productMapper::toDomain );
   }

   public Page< Product > getProductsByCategory( String category, int page, int size ) {
      return productRepository.findAllByCategoryId( category, PageRequest.of( page, size ) )
            .map( productMapper::toDomain );
   }

   public Product getProduct( String id ) {
      Supplier< Product > findByProductInDb = () -> productRepository.findById( UUID.fromString( id ) )
            .map( productMapper::toDomain )
            .orElseThrow( () -> new RuntimeException( "Product not found with id: " + id ) );

      return productCacheService.getCachedOrCompute( id, findByProductInDb );
   }

   public Product createProduct( CreateProductDTO createProductDTO ) {
      var product = Product.create( createProductDTO );
      var entity = productMapper.toEntity( product );
      var saved = productRepository.save( entity );
      return productMapper.toDomain( saved );
   }

}