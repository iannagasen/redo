package dev.agasen.core.product;

import dev.agasen.core.product.persistence.ProductRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

public record DataInitializer(
    ProductRepository productRepository
) {

    @Bean
    ApplicationRunner init(ProductRepository repository) {
        return args -> {

        };
    }

}

