package dev.agasen.core.product;

import dev.agasen.core.product.persistence.ProductEntity;
import dev.agasen.core.product.persistence.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Configuration
@AllArgsConstructor
public class DataInitializer {

   private final ProductRepository productRepository;

   @Bean
   ApplicationRunner init( ProductRepository repository ) {
      return args -> seedData();
   }

   void seedData() {
      productRepository.deleteAll();

      productRepository.save( new ProductEntity(
            UUID.randomUUID(),
            "iPhone 15 Pro",
            "Latest iPhone with advanced features",
            "IPHONE-15-PRO-128",
            "iphone-15-pro",
            "Apple",
            new BigDecimal( "999.99" ),
            "USD",
            50,
            Map.of( "color", "Space Black", "storage", "128GB", "screen_size", "6.1 inches" ),
            UUID.randomUUID(),
            Instant.now(),
            Instant.now()
      ) );

      productRepository.save( new ProductEntity(
            UUID.randomUUID(),
            "Cotton T-Shirt",
            "Comfortable cotton t-shirt",
            "COTTON-TSHIRT-M-BLUE",
            "cotton-tshirt-blue",
            "Generic",
            new BigDecimal( "19.99" ),
            "USD",
            100,
            Map.of( "size", "M", "color", "Blue", "material", "100% Cotton" ),
            UUID.randomUUID(),
            Instant.now(),
            Instant.now()
      ) );

      productRepository.save( new ProductEntity(
            UUID.randomUUID(),
            "The Great Gatsby",
            "Classic American novel",
            "BOOK-GATSBY-PB",
            "great-gatsby",
            "Scribner",
            new BigDecimal( "12.99" ),
            "USD",
            25,
            Map.of( "author", "F. Scott Fitzgerald", "pages", 180, "format", "Paperback" ),
            UUID.randomUUID(),
            Instant.now(),
            Instant.now()
      ) );
   }

}

