package dev.agasen.core.product.domain;

import dev.agasen.core.product.persistence.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public record ProductService(
        ProductRepository productRepository,
        ProductMapper productMapper
) {

    public List<Product> getProducts() {
        var entities = productRepository.findAll();
        return productMapper.toDomain(entities);
    }
}