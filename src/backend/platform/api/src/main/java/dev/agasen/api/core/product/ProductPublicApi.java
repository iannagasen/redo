package dev.agasen.api.core.product;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange( "/public/api/v1/products" )
public interface ProductPublicApi {

   @GetExchange( "/brands" )
   List< String > getBrands(
      @RequestParam( "q" ) String query,
      @RequestParam( defaultValue = "0", name = "p" ) int page,
      @RequestParam( defaultValue = "10", name = "s" ) int size
   );

}
