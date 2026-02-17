package dev.agasen.core.product.infrastructure.rest;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.common.pagination.PagedResult;
import dev.agasen.core.product.application.read.BrandRetrievalService;
import dev.agasen.core.product.application.read.ProductRetrievalService;
import dev.agasen.core.product.application.write.ProductCreationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( "/api/v1/products" )
@Validated
@SecurityRequirement( name = "oauth2" )
@Slf4j
@RequiredArgsConstructor
public class ProductController {

   private final ProductCreationService productCreationService;
   private final ProductRetrievalService productRetrievalService;
   private final BrandRetrievalService brandRetrievalService;

   @GetMapping
   public PagedResult< ProductDetails > getProducts(
      @RequestParam( defaultValue = "0", name = "page" ) @Min( 0 ) int page,
      @RequestParam( defaultValue = "20", name = "size" ) @Min( 1 ) @Max( 100 ) int size
   ) {
      log.info( "User is {}", SecurityContextHolder.getContext().getAuthentication() );
      return productRetrievalService.getAllProducts( page, size );
   }

   @GetMapping( "/brands" )
   public List< String > getBrands(
      @RequestParam( "q" ) String query,
      @RequestParam( defaultValue = "0", name = "p" ) @Min( 0 ) int page,
      @RequestParam( defaultValue = "10", name = "s" ) @Min( 1 ) @Max( 100 ) int size
   ) {
      return brandRetrievalService.getBrands( query, page, size );
   }


//   @GetMapping( "/{id}" )
//   public ProductDetails getProduct( @PathVariable Long id ) {
//      return productService.getProduct( id );
//   }
//
//   @GetMapping( "/category/{category}" )
//   public Page< ProductDetails > getProductsByCategory(
//      @PathVariable String category,
//      @RequestParam( defaultValue = "0" ) @Min( 0 ) int page,
//      @RequestParam( defaultValue = "1" ) @Min( 1 ) @Max( 100 ) int size
//   ) {
//      return productService.getProductsByCategory( category, page, size );
//   }

   @PostMapping
   public ProductDetails addProduct( @RequestBody ProductCreationDetails productCreationDetails ) {
      return productCreationService.create( productCreationDetails );
   }

    /*
    Product Management:
    GET /api/products - List products (with pagination, filtering)
    GET /api/products/{id} - Get single product
    POST /api/products - Create product (admin)
    PUT /api/products/{id} - Update product (admin)
    DELETE /api/products/{id} - Delete product (admin)
    GET /api/products/search?q={query} - Search products
    Category Management:
    GET /api/categories - List categories
    GET /api/categories/{id} - Get category with products
    POST /api/categories - Create category (admin)
    PUT /api/categories/{id} - Update category (admin)
    Inventory:
    GET /api/products/{id}/inventory - Check stock
    PUT /api/products/{id}/inventory - Update stock (admin)
     */
}
