package dev.agasen.core.product;

import dev.agasen.core.product.domain.ProductService;
import dev.agasen.core.product.persistence.*;
import dev.agasen.core.product.persistence.entity.*;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@AllArgsConstructor
public class DataInitializer {

   private final AttributeModelRepository attributeModelRepository;
   private final ProductModelRepository productModelRepository;
   private final AttributeSetRepository attributeSetRepository;
   private final ProductRepository productRepository;
   private final AttributeRepository attributeRepository;
   private final ProductService productService;

   @Bean
   @Order( 1 )
   ApplicationRunner init() {
      return args -> {
         productRepository.deleteAll();
         attributeModelRepository.deleteAll();
         productModelRepository.deleteAll();
         attributeRepository.deleteAll();
         attributeSetRepository.deleteAll();

         createTestData();
      };
   }

   @Bean
   @Order( 2 )
   @Transactional
   ApplicationRunner init2() {
      return args -> {
         productService.getAllProducts();
      };
   }


   public void createTestData() {

      // ====================================================================
      // 1. CREATE AND SAVE ATTRIBUTE MODELS
      // ====================================================================

      List< AttributeModel > attributeModels = Arrays.asList(
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440001" ),
                  "Color", "color", DataType.STRING, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440002" ),
                  "Storage Capacity", "storage", DataType.STRING, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440003" ),
                  "RAM", "ram", DataType.STRING, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440004" ),
                  "Screen Size", "screen_size", DataType.DECIMAL, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440005" ),
                  "Battery Life", "battery_life", DataType.INTEGER, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440006" ),
                  "Weight", "weight", DataType.DECIMAL, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440007" ),
                  "Wireless", "wireless", DataType.BOOLEAN, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440008" ),
                  "Size", "size", DataType.STRING, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440009" ),
                  "Warranty Period", "warranty", DataType.INTEGER, new ArrayList<>()
            ),
            new AttributeModel(
                  UUID.fromString( "550e8400-e29b-41d4-a716-446655440010" ),
                  "Release Date", "release_date", DataType.DATE, new ArrayList<>()
            )
      );

      List< AttributeModel > savedAttributeModels = attributeModelRepository.saveAll( attributeModels );
      System.out.println( "Saved " + savedAttributeModels.size() + " AttributeModels" );

      // Get references for later use
      AttributeModel colorAttr = savedAttributeModels.get( 0 );
      AttributeModel storageAttr = savedAttributeModels.get( 1 );
      AttributeModel ramAttr = savedAttributeModels.get( 2 );
      AttributeModel screenSizeAttr = savedAttributeModels.get( 3 );
      AttributeModel batteryAttr = savedAttributeModels.get( 4 );
      AttributeModel weightAttr = savedAttributeModels.get( 5 );
      AttributeModel wirelessAttr = savedAttributeModels.get( 6 );
      AttributeModel sizeAttr = savedAttributeModels.get( 7 );
      AttributeModel warrantyAttr = savedAttributeModels.get( 8 );
      AttributeModel releaseDateAttr = savedAttributeModels.get( 9 );

      // ====================================================================
      // 2. CREATE AND SAVE PRODUCT MODELS
      // ====================================================================

      List< ProductModel > productModels = Arrays.asList(
            new ProductModel(
                  UUID.fromString( "660e8400-e29b-41d4-a716-446655440001" ),
                  "Smartphone", "smartphone", "Mobile phone with advanced features",
                  null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            ),
            new ProductModel(
                  UUID.fromString( "660e8400-e29b-41d4-a716-446655440002" ),
                  "Laptop", "laptop", "Portable personal computer",
                  null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            ),
            new ProductModel(
                  UUID.fromString( "660e8400-e29b-41d4-a716-446655440003" ),
                  "Headphones", "headphones", "Audio device worn over ears",
                  null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            ),
            new ProductModel(
                  UUID.fromString( "660e8400-e29b-41d4-a716-446655440004" ),
                  "Smartwatch", "smartwatch", "Wearable computing device",
                  null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            )
      );

      List< ProductModel > savedProductModels = productModelRepository.saveAll( productModels );
      System.out.println( "Saved " + savedProductModels.size() + " ProductModels" );

      // Get references for later use
      ProductModel smartphoneModel = savedProductModels.get( 0 );
      ProductModel laptopModel = savedProductModels.get( 1 );
      ProductModel headphonesModel = savedProductModels.get( 2 );
      ProductModel smartwatchModel = savedProductModels.get( 3 );

      // ====================================================================
      // 3. CREATE AND SAVE ATTRIBUTE SETS
      // ====================================================================

      List< AttributeSet > attributeSets = Arrays.asList(
            // Smartphone AttributeSets
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440001" ),
                  smartphoneModel, colorAttr, true, "Body Color", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440002" ),
                  smartphoneModel, storageAttr, true, "Storage Capacity", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440003" ),
                  smartphoneModel, screenSizeAttr, false, "Screen Size", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440004" ),
                  smartphoneModel, batteryAttr, false, "Battery Life", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440005" ),
                  smartphoneModel, releaseDateAttr, false, "Release Date", new ArrayList<>()
            ),

            // Laptop AttributeSets
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440006" ),
                  laptopModel, colorAttr, true, "Chassis Color", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440007" ),
                  laptopModel, ramAttr, true, "Memory (RAM)", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440008" ),
                  laptopModel, storageAttr, true, "Storage Drive", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440009" ),
                  laptopModel, screenSizeAttr, false, "Display Size", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440010" ),
                  laptopModel, weightAttr, false, "Weight", new ArrayList<>()
            ),

            // Headphones AttributeSets
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440011" ),
                  headphonesModel, colorAttr, true, "Color", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440012" ),
                  headphonesModel, wirelessAttr, true, "Wireless Connection", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440013" ),
                  headphonesModel, batteryAttr, false, "Playback Time", new ArrayList<>()
            ),

            // Smartwatch AttributeSets
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440014" ),
                  smartwatchModel, colorAttr, true, "Case Color", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440015" ),
                  smartwatchModel, sizeAttr, true, "Case Size", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440016" ),
                  smartwatchModel, batteryAttr, false, "Battery Life", new ArrayList<>()
            ),
            new AttributeSet(
                  UUID.fromString( "770e8400-e29b-41d4-a716-446655440017" ),
                  smartwatchModel, warrantyAttr, false, "Warranty", new ArrayList<>()
            )
      );

      List< AttributeSet > savedAttributeSets = attributeSetRepository.saveAll( attributeSets );
      System.out.println( "Saved " + savedAttributeSets.size() + " AttributeSets" );

      // ====================================================================
      // 4. CREATE AND SAVE PRODUCTS
      // ====================================================================

      List< Product > products = Arrays.asList(
            new Product(
                  UUID.fromString( "880e8400-e29b-41d4-a716-446655440001" ),
                  "iPhone 15 Pro", "Latest Apple smartphone with titanium design and A17 Pro chip",
                  "APL-IP15P-256-SB", "iphone-15-pro-space-black-256gb", "Apple",
                  new BigDecimal( "999.00" ), "USD", 45, 127, 8, smartphoneModel, new ArrayList<>()
            ),
            new Product(
                  UUID.fromString( "880e8400-e29b-41d4-a716-446655440002" ),
                  "MacBook Air 13\" M3", "Ultra-thin laptop with M3 chip, all-day battery life",
                  "APL-MBA13-M3-512-MN", "macbook-air-13-m3-midnight-512gb", "Apple",
                  new BigDecimal( "1299.00" ), "USD", 23, 89, 3, laptopModel, new ArrayList<>()
            ),
            new Product(
                  UUID.fromString( "880e8400-e29b-41d4-a716-446655440003" ),
                  "Sony WH-1000XM5", "Premium noise canceling wireless headphones with industry-leading noise cancellation",
                  "SNY-WH1000XM5-BLK", "sony-wh-1000xm5-black", "Sony",
                  new BigDecimal( "399.99" ), "USD", 67, 234, 12, headphonesModel, new ArrayList<>()
            ),
            new Product(
                  UUID.fromString( "880e8400-e29b-41d4-a716-446655440004" ),
                  "Apple Watch Series 9", "Advanced health and fitness tracking smartwatch with S9 chip",
                  "APL-AWS9-44-SG", "apple-watch-series-9-space-gray-44mm", "Apple",
                  new BigDecimal( "429.00" ), "USD", 34, 156, 7, smartwatchModel, new ArrayList<>()
            )
      );

      List< Product > savedProducts = productRepository.saveAll( products );
      System.out.println( "Saved " + savedProducts.size() + " Products" );

      // ====================================================================
      // 5. CREATE AND SAVE ATTRIBUTES (Product attribute values)
      // ====================================================================

      List< Attribute > attributes = Arrays.asList(
            // iPhone 15 Pro Attributes
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440001" ),
                  savedProducts.get( 0 ), savedAttributeSets.get( 0 ), // iPhone + Color
                  DataType.STRING, "Space Black", "Space Black", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440002" ),
                  savedProducts.get( 0 ), savedAttributeSets.get( 1 ), // iPhone + Storage
                  DataType.STRING, "256GB", "256GB", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440003" ),
                  savedProducts.get( 0 ), savedAttributeSets.get( 2 ), // iPhone + Screen
                  DataType.DECIMAL, "6.1", null, null, new BigDecimal( "6.1" ), null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440004" ),
                  savedProducts.get( 0 ), savedAttributeSets.get( 3 ), // iPhone + Battery
                  DataType.INTEGER, "29", null, 29, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440005" ),
                  savedProducts.get( 0 ), savedAttributeSets.get( 4 ), // iPhone + Release Date
                  DataType.DATE, "2023-09-22", null, null, null, null, LocalDate.of( 2023, 9, 22 )
            ),

            // MacBook Air M3 Attributes
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440006" ),
                  savedProducts.get( 1 ), savedAttributeSets.get( 5 ), // MacBook + Color
                  DataType.STRING, "Midnight", "Midnight", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440007" ),
                  savedProducts.get( 1 ), savedAttributeSets.get( 6 ), // MacBook + RAM
                  DataType.STRING, "16GB", "16GB", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440008" ),
                  savedProducts.get( 1 ), savedAttributeSets.get( 7 ), // MacBook + Storage
                  DataType.STRING, "512GB SSD", "512GB SSD", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440009" ),
                  savedProducts.get( 1 ), savedAttributeSets.get( 8 ), // MacBook + Screen
                  DataType.DECIMAL, "13.6", null, null, new BigDecimal( "13.6" ), null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440010" ),
                  savedProducts.get( 1 ), savedAttributeSets.get( 9 ), // MacBook + Weight
                  DataType.DECIMAL, "1.24", null, null, new BigDecimal( "1.24" ), null, null
            ),

            // Sony WH-1000XM5 Attributes
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440011" ),
                  savedProducts.get( 2 ), savedAttributeSets.get( 10 ), // Sony + Color
                  DataType.STRING, "Black", "Black", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440012" ),
                  savedProducts.get( 2 ), savedAttributeSets.get( 11 ), // Sony + Wireless
                  DataType.BOOLEAN, "true", null, null, null, true, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440013" ),
                  savedProducts.get( 2 ), savedAttributeSets.get( 12 ), // Sony + Battery
                  DataType.INTEGER, "30", null, 30, null, null, null
            ),

            // Apple Watch Series 9 Attributes
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440014" ),
                  savedProducts.get( 3 ), savedAttributeSets.get( 13 ), // Watch + Color
                  DataType.STRING, "Space Gray", "Space Gray", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440015" ),
                  savedProducts.get( 3 ), savedAttributeSets.get( 14 ), // Watch + Size
                  DataType.STRING, "44mm", "44mm", null, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440016" ),
                  savedProducts.get( 3 ), savedAttributeSets.get( 15 ), // Watch + Battery
                  DataType.INTEGER, "18", null, 18, null, null, null
            ),
            new Attribute(
                  UUID.fromString( "990e8400-e29b-41d4-a716-446655440017" ),
                  savedProducts.get( 3 ), savedAttributeSets.get( 16 ), // Watch + Warranty
                  DataType.INTEGER, "12", null, 12, null, null, null
            )
      );

      List< Attribute > savedAttributes = attributeRepository.saveAll( attributes );
      System.out.println( "Saved " + savedAttributes.size() + " Attributes" );

      // ====================================================================
      // 6. SUMMARY
      // ====================================================================

      System.out.println( "\n=== EAV TEST DATA CREATION COMPLETE ===" );
      System.out.println( "AttributeModels: " + savedAttributeModels.size() );
      System.out.println( "ProductModels: " + savedProductModels.size() );
      System.out.println( "AttributeSets: " + savedAttributeSets.size() );
      System.out.println( "Products: " + savedProducts.size() );
      System.out.println( "Attributes: " + savedAttributes.size() );
      System.out.println( "=======================================" );
   }


//   List< ProductCreationDetails > createProducts() {
//      return List.of(
//            new ProductCreationDetails(
//                  "iPhone 15 Pro",
//                  "Latest iPhone with advanced features",
//                  "IPHONE-15-PRO-128",
//                  "iphone-15-pro",
//                  "Apple",
//                  new BigDecimal( "999.99" ),
//                  "USD",
//                  50,
//                  Map.of( "color", "Space Black", "storage", "128GB", "screen_size", "6.1 inches" )
//            ),
//            new ProductCreationDetails(
//                  "Cotton T-Shirt",
//                  "Comfortable cotton t-shirt",
//                  "COTTON-TSHIRT-M-BLUE",
//                  "cotton-tshirt-blue",
//                  "Generic",
//                  new BigDecimal( "19.99" ),
//                  "USD",
//                  100,
//                  Map.of( "size", "M", "color", "Blue", "material", "100% Cotton" )
//            ),
//            new ProductCreationDetails(
//                  "The Great Gatsby",
//                  "Classic American novel",
//                  "BOOK-GATSBY-PB",
//                  "great-gatsby",
//                  "Scribner",
//                  new BigDecimal( "12.99" ),
//                  "USD",
//                  25,
//                  Map.of( "author", "F. Scott Fitzgerald", "pages", 180, "format", "Paperback" )
//            )
//      );
//   }

}

