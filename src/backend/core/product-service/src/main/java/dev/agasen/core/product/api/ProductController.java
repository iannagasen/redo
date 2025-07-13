package dev.agasen.core.product.api;

import dev.agasen.core.product.domain.Product;
import dev.agasen.core.product.domain.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/v1/products")
public record ProductController(
        ProductService productService
) {

    @GetMapping
    public List<Product> getProducts() {
        return productService.getProducts();
    }
}
