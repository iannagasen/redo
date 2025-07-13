package dev.agasen.core.product.persistence;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface ProductRepository extends ListCrudRepository<ProductEntity, UUID> {

}
