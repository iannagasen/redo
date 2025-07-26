package dev.agasen.core.product.domain;

import dev.agasen.common.cache.CachingService;
import dev.agasen.core.product.persistence.ProductRepository;
import dev.agasen.core.product.persistence.entity.Product;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
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
      var productModel = products.get( 0 ).getProductModel();
      System.out.println( productModel );  // will now work
      System.out.println( products );
      return products;
   }

   public Page< ProductDetails > getProducts( int page, int size ) {
      return productRepository.findAll( PageRequest.of( page, size ) )
            .map( productMapper::toDomain );
   }

   public Page< ProductDetails > getProductsByCategory( String category, int page, int size ) {
//      return productRepository.findAllByCategoryId( UUID.fromString( category ), PageRequest.of( page, size ) )
//            .map( productMapper::toDomain );
      return null;
   }

   public ProductDetails getProduct( String id ) {
      Supplier< ProductDetails > findByProductInDb = () -> productRepository.findById( UUID.fromString( id ) )
            .map( productMapper::toDomain )
            .orElseThrow( () -> new RuntimeException( "Product not found with id: " + id ) );

      return productCachingService.getCachedOrCompute( id, findByProductInDb );
   }

   public ProductDetails createProduct( ProductCreationDetails productCreationDetails ) {
      var entity = productMapper.toEntity( productCreationDetails );
      var saved = productRepository.save( entity );
      return productMapper.toDomain( saved );
   }

}