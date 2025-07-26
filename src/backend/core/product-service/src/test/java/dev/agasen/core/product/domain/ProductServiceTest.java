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
class ProductServiceTest {

   @Mock
   private ProductRepository productRepository;

   @Mock
   private ProductMapper productMapper;

   @Mock
   private CachingService< String, Product > cachingService;

   @InjectMocks
   private ProductService sut;

   @Test
   void testGetProduct_whenInCache_thenCachedProduct() {
      // setup
      String id = UUID.randomUUID().toString();
      Product cachedProduct = createTestProduct();
      when( cachingService.getCachedOrCompute( eq( id ), any() ) )
            .thenReturn( cachedProduct );

      // execute
      Product result = sut.getProduct( id );

      // assert
      assertEquals( cachedProduct, result );
      verify( cachingService ).getCachedOrCompute( eq( id ), any() );
      verifyNoInteractions( productRepository );
      verifyNoInteractions( productMapper );
   }

   private Product createTestProduct() {
      return new Product(
            UUID.fromString( "11111111-1111-1111-1111-111111111111" ),
            "Test Product",
            "This is a test product.",
            "SKU-TEST-123",
            "test-product",
            "Test Brand",
            new BigDecimal( "99.99" ),
            "USD",
            100,
            true,
            false,
            Map.of( "color", "red", "size", "M" ),
            UUID.fromString( "22222222-2222-2222-2222-222222222222" ),
            Instant.parse( "2023-01-01T00:00:00Z" ),
            Instant.parse( "2023-01-02T00:00:00Z" )
      );
   }
}