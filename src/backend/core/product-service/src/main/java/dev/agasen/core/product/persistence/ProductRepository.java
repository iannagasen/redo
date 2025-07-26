package dev.agasen.core.product.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository< ProductEntity, UUID > {

   Page< ProductEntity > findAllByCategoryId( UUID categoryId, Pageable pageable );

}
