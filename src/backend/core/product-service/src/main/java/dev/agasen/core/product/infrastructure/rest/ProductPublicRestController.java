package dev.agasen.core.product.infrastructure.rest;

import dev.agasen.core.product.application.read.BrandRetrievalService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping( "/public/api/v1/products" )
@Validated
@Slf4j
@RequiredArgsConstructor
public class ProductPublicRestController {

   private final BrandRetrievalService brandRetrievalService;

   @GetMapping( "/brands" )
   public List< String > getBrands(
      @RequestParam( "q" ) String query,
      @RequestParam( defaultValue = "0", name = "p" ) @Min( 0 ) int page,
      @RequestParam( defaultValue = "10", name = "s" ) @Min( 1 ) @Max( 100 ) int size
   ) {
      return brandRetrievalService.getBrands( query, page, size );
   }

}
