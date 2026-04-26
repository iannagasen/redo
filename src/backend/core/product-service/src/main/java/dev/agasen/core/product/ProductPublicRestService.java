package dev.agasen.core.product;

import dev.agasen.platform.contracts.core.product.ProductPublicApi;
import dev.agasen.core.product.application.read.BrandRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class ProductPublicRestService implements ProductPublicApi {

   private final BrandRetrievalService brandRetrievalService;

   @Override
   public List< String > getBrands( String query, int page, int size ) {
      return brandRetrievalService.getBrands( query, page, size );
   }
}
