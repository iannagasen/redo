package dev.agasen.core.product.persistence;

import dev.agasen.core.product.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository< Product, UUID > {

//   Page< ProductEntity > findAllByCategoryId( UUID categoryId, Pageable pageable );

}
