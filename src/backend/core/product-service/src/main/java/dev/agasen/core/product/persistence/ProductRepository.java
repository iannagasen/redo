package dev.agasen.core.product.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProductRepository extends ListCrudRepository< ProductEntity, UUID >,
      PagingAndSortingRepository< ProductEntity, UUID > {

   Page< ProductEntity > findAllByCategoryId( String categoryId, Pageable pageable );

}
