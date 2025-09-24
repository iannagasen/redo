package dev.agasen.core.product.application.read;

import dev.agasen.core.product.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
   public List< String > getBrands( String query, int size ) {
      return productRepository.findDistinctBrandsByQuery( query, PageRequest.of( 0, size ) );
   }

   public List< String > getBrands( String query, int page, int size ) {
      return productRepository.findDistinctBrandsByQuery( query, PageRequest.of( page, size ) );
   }

}
