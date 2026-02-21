package dev.agasen.core.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SeedDataIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testInitialProductsAndCategoriesPresence() {
        // Verify Categories
        Integer categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
        log.info("Checking categories table: found {} categories", categoryCount);
        assertThat(categoryCount).isGreaterThanOrEqualTo(2);
        
        String electronicsName = jdbcTemplate.queryForObject(
            "SELECT name FROM categories WHERE name = 'Electronics'", 
            String.class
        );
        log.info("Checking for 'Electronics' category: found name '{}'", electronicsName);
        assertThat(electronicsName).isEqualTo("Electronics");

        // Verify Products
        Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        log.info("Checking products table: found {} products", productCount);
        assertThat(productCount).isGreaterThanOrEqualTo(3);

        String productSku = jdbcTemplate.queryForObject(
            "SELECT sku FROM products WHERE name = 'Smartphone X'", 
            String.class
        );
        log.info("Checking product 'Smartphone X': found sku '{}'", productSku);
        assertThat(productSku).isEqualTo("SM-X-001");
    }
}
