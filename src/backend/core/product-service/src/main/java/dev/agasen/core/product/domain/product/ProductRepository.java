package dev.agasen.core.product.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository< Product, Long > {

//   Page< ProductEntity > findAllByCategoryId( UUID categoryId, Pageable pageable );

}
