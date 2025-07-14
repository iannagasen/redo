package dev.agasen.core.product.api;

import dev.agasen.core.product.domain.CreateProductDTO;
import dev.agasen.core.product.domain.Product;
import dev.agasen.core.product.domain.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<Product> getProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int size
    ) {
        return productService.getProducts(page, size);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @GetMapping("/category/{category}")
    public Page<Product> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int size
    ) {
        return productService.getProductsByCategory(category, page, size);
    }

    @PostMapping
    public Product addProduct(@RequestBody CreateProductDTO createProductDTO) {
        return productService.createProduct(createProductDTO);
    }

    /*
    Product Management:
    GET /api/products - List products (with pagination, filtering)
    GET /api/products/{id} - Get single product
    POST /api/products - Create product (admin)
    PUT /api/products/{id} - Update product (admin)
    DELETE /api/products/{id} - Delete product (admin)
    GET /api/products/search?q={query} - Search products
    Category Management:
    GET /api/categories - List categories
    GET /api/categories/{id} - Get category with products
    POST /api/categories - Create category (admin)
    PUT /api/categories/{id} - Update category (admin)
    Inventory:
    GET /api/products/{id}/inventory - Check stock
    PUT /api/products/{id}/inventory - Update stock (admin)
     */
}
