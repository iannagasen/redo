package dev.agasen.core.product;

import dev.agasen.platform.contracts.core.product.ProductApi;
import dev.agasen.platform.contracts.core.product.product.ProductCreationDetails;
import dev.agasen.platform.contracts.core.product.product.ProductDetails;
import dev.agasen.platform.core.http.pagination.PagedResult;
import dev.agasen.core.product.application.read.BrandRetrievalService;
import dev.agasen.core.product.application.read.ProductRetrievalService;
import dev.agasen.core.product.application.write.ProductCreationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@SecurityRequirement( name = "oauth2" )
@Slf4j
@RequiredArgsConstructor
public class ProductRestService implements ProductApi {

   private final ProductCreationService productCreationService;
   private final ProductRetrievalService productRetrievalService;
   private final BrandRetrievalService brandRetrievalService;

   @Override
   public List< ProductDetails > getProducts( List< Long > productIds ) {
      return productRetrievalService.getProductsByIds( productIds );
   }

   @Override
   public ProductDetails getProduct( Long id ) {
      return productRetrievalService.getProductById( id );
   }

   @Override
   public PagedResult< ProductDetails > getProducts( int page, int size ) {
      log.info( "User is {}", SecurityContextHolder.getContext().getAuthentication() );
      return productRetrievalService.getAllProducts( page, size );
   }

   @Override
   public List< String > getBrands( String query, int page, int size ) {
      return brandRetrievalService.getBrands( query, page, size );
   }

   @Override
   public ProductDetails addProduct( ProductCreationDetails productCreationDetails ) {
      return productCreationService.create( productCreationDetails );
   }
}
