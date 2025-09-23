package dev.agasen.core.product.infrastructure.rest;

import dev.agasen.core.product.application.read.BrandRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
   public List< String > getBrands() {
      return brandRetrievalService.getAllBrands();
   }

}
