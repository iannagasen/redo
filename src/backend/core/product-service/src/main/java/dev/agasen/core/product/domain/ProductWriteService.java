package dev.agasen.core.product.domain;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.api.product.product.ProductModificationDetails;
import dev.agasen.api.product.product.ProductStatusUpdate;
import dev.agasen.core.product.mapper.ProductMapper;
import dev.agasen.core.product.persistence.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductWriteService {

   private final ProductRepository productRepository;
   private final ProductMapper productMapper;

   @PreAuthorize( "hasAuthority('SCOPE_product:write-create')" )
   public Long create( ProductCreationDetails dto ) {
      var entity = productMapper.toEntity( dto );
      var product = productRepository.save( entity );
      return product.getId();
   }

   @PreAuthorize( "hasAuthority('SCOPE_product:write-update')" )
   public Long update( ProductModificationDetails dto ) {
      throw new UnsupportedOperationException( "Not supported yet." );
   }

   public Long update( ProductStatusUpdate dto ) {
      return null;
   }
}
