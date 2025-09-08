package dev.agasen.core.product;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.core.product.domain.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "/api/v1/products" )
@Validated
//@SecurityRequirement( name = "oauth2" )
@Slf4j
public class ProductController {

   private final ProductService productService;

   public ProductController( ProductService productService ) {
      this.productService = productService;
   }

   @GetMapping
   public Page< ProductDetails > getProducts(
         @RequestParam( defaultValue = "0", name = "page" ) @Min( 0 ) int page,
         @RequestParam( defaultValue = "1", name = "size" ) @Min( 1 ) @Max( 100 ) int size
   ) {
      log.info( "User is {}", SecurityContextHolder.getContext().getAuthentication() );
      return productService.getProducts( page, size );
   }

   @GetMapping( "/{id}" )
   public ProductDetails getProduct( @PathVariable Long id ) {
      return productService.getProduct( id );
   }

   @GetMapping( "/category/{category}" )
   public Page< ProductDetails > getProductsByCategory(
         @PathVariable String category,
         @RequestParam( defaultValue = "0" ) @Min( 0 ) int page,
         @RequestParam( defaultValue = "1" ) @Min( 1 ) @Max( 100 ) int size
   ) {
      return productService.getProductsByCategory( category, page, size );
   }

   @PostMapping
   public ProductDetails addProduct( @RequestBody ProductCreationDetails productCreationDetails ) {
      return productService.createProduct( productCreationDetails );
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
