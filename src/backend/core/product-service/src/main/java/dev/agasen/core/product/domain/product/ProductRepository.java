package dev.agasen.core.product.domain.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository< Product, Long > {

//   Page< ProductEntity > findAllByCategoryId( UUID categoryId, Pageable pageable );

   @Query( """
         select distinct p.brand
         from Product p
         where lower(p.brand) like lower(concat(:query, '%') )
      """ )
   List< String > findDistinctBrandsByQuery( @Param( "query" ) String query, Pageable pageable );

}
