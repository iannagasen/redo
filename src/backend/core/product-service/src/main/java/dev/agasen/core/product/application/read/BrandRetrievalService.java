package dev.agasen.core.product.application.read;

import dev.agasen.core.product.domain.product.Product;
import dev.agasen.core.product.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional( readOnly = true )
@Service
@RequiredArgsConstructor
public class BrandRetrievalService {

   private final ProductRepository productRepository;

   // TODO: a better way
//   @PreAuthorize( """
//          hasAuthority('SCOPE_product:write-read') or
//          hasAuthority('SCOPE_openid')
//      """ )
   public List< String > getAllBrands() {
      return productRepository.findAll().stream().map( Product::getBrand ).toList();
   }

}
