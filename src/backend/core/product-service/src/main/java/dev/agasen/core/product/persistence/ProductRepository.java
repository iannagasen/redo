package dev.agasen.core.product.persistence;

import dev.agasen.core.product.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository< Product, Long > {

//   Page< ProductEntity > findAllByCategoryId( UUID categoryId, Pageable pageable );

}
