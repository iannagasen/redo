package dev.agasen.core.product.domain;

import dev.agasen.common.cache.CachingService;
import dev.agasen.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
class ProductDetailsServiceTest {

   @Mock
   private ProductRepository productRepository;

   @Mock
   private ProductMapper productMapper;

   @Mock
   private CachingService< String, ProductDetails > cachingService;

   @InjectMocks
   private ProductService sut;

   @Test
   void testGetProduct_whenInCache_thenCachedProduct() {
      // setup
      String id = UUID.randomUUID().toString();
      ProductDetails cachedProductDetails = createTestProduct();
      when( cachingService.getCachedOrCompute( eq( id ), any() ) )
            .thenReturn( cachedProductDetails );

      // execute
      ProductDetails result = sut.getProduct( id );

      // assert
      assertEquals( cachedProductDetails, result );
      verify( cachingService ).getCachedOrCompute( eq( id ), any() );
      verifyNoInteractions( productRepository );
      verifyNoInteractions( productMapper );
   }

   private ProductDetails createTestProduct() {
      return new ProductDetails(
            UUID.fromString( "11111111-1111-1111-1111-111111111111" ),
            "Test Product",
            "This is a test product.",
            "SKU-TEST-123",
            "test-product",
            "Test Brand",
            new BigDecimal( "99.99" ),
            "USD",
            100,
            Map.of( "color", "red", "size", "M" ),
            Instant.parse( "2023-01-01T00:00:00Z" ),
            Instant.parse( "2023-01-02T00:00:00Z" )
      );
   }
}