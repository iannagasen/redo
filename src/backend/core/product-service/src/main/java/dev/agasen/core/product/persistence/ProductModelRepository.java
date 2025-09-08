package dev.agasen.core.product.persistence;

import dev.agasen.core.product.persistence.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductModelRepository extends JpaRepository< ProductModel, Long > {
}
