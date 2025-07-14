package dev.agasen.core.product.domain;

import dev.agasen.core.product.persistence.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public record ProductService(
      ProductRepository productRepository,
      ProductMapper productMapper
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
      UUID uuid = UUID.fromString( id );
      return productRepository.findById( uuid )
            .map( productMapper::toDomain )
            .orElseThrow( () -> new RuntimeException( "Product not found with id: " + id ) );
   }

   public Product createProduct( CreateProductDTO createProductDTO ) {
      var product = Product.create( createProductDTO );
      var entity = productMapper.toEntity( product );
      var saved = productRepository.save( entity );
      return productMapper.toDomain( saved );
   }

}