package dev.agasen.api.core.product;

import dev.agasen.api.core.product.product.ProductCreationDetails;
import dev.agasen.api.core.product.product.ProductDetails;
import dev.agasen.common.http.pagination.PagedResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange( "/api/v1/products" )
public interface ProductApi {

   @GetExchange( "/{id}" )
   ProductDetails getProduct( @PathVariable Long id );

   @GetExchange( "/batch" )
   List< ProductDetails > getProducts( @RequestParam( "ids" ) List< Long > productIds );

   @GetExchange
   PagedResult< ProductDetails > getProducts(
      @RequestParam( defaultValue = "0", name = "page" ) int page,
      @RequestParam( defaultValue = "20", name = "size" ) int size
   );

   @GetExchange( "/brands" )
   List< String > getBrands(
      @RequestParam( "q" ) String query,
      @RequestParam( defaultValue = "0", name = "p" ) int page,
      @RequestParam( defaultValue = "10", name = "s" ) int size
   );

   @PostExchange
   ProductDetails addProduct( @RequestBody @Valid ProductCreationDetails productCreationDetails );

}
