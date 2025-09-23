package dev.agasen.core.product;

import dev.agasen.core.product.domain.category.Category;
import dev.agasen.core.product.domain.category.CategoryRepository;
import dev.agasen.core.product.domain.persistence.entity.ProductModel;
import dev.agasen.core.product.domain.product.Product;
import dev.agasen.core.product.domain.product.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@AllArgsConstructor
@Slf4j
public class DataInitializer {

   private final ProductRepository productRepository;
   //   private final ProductModelRepository productModelRepository;
   private final CategoryRepository categoryRepository;


   @Bean
   public ApplicationRunner init() {
      return args -> {
         if ( shouldSkipInitialization() ) {
            log.info( "Sample data already exists, skipping initialization" );
            return;
         }

         log.info( "Initializing sample data..." );

         // Create Categories
         List< Category > categories = createCategories();

         // Create Product Models
         List< ProductModel > productModels = createProductModels();

         // Create Products
         createProducts( productModels );
      };
   }

   private boolean shouldSkipInitialization() {
      // Skip if any data already exists
      return categoryRepository.count() > 0 ||
//             productModelRepository.count() > 0 ||
             productRepository.count() > 0;
   }

   private List< Category > createCategories() {
      log.info( "Creating categories..." );

      // Root categories
      Category electronics = createAndSaveCategory( "Electronics", "Electronic devices and accessories", null );
      Category clothing = createAndSaveCategory( "Clothing", "Fashion and apparel", null );
      Category books = createAndSaveCategory( "Books", "Books and literature", null );
      Category sports = createAndSaveCategory( "Sports & Outdoors", "Sports equipment and outdoor gear", null );

      // Electronics subcategories
      Category smartphones = createAndSaveCategory( "Smartphones", "Mobile phones and accessories", electronics );
      Category laptops = createAndSaveCategory( "Laptops", "Portable computers", electronics );
      Category tablets = createAndSaveCategory( "Tablets", "Tablet computers", electronics );
      Category accessories = createAndSaveCategory( "Accessories", "Electronic accessories", electronics );

      // Clothing subcategories
      Category menClothing = createAndSaveCategory( "Men's Clothing", "Clothing for men", clothing );
      Category womenClothing = createAndSaveCategory( "Women's Clothing", "Clothing for women", clothing );
      Category shoes = createAndSaveCategory( "Shoes", "Footwear", clothing );

      // Books subcategories
      Category fiction = createAndSaveCategory( "Fiction", "Fiction books", books );
      Category nonFiction = createAndSaveCategory( "Non-Fiction", "Non-fiction books", books );
      Category technical = createAndSaveCategory( "Technical", "Technical and programming books", books );

      return List.of( electronics, clothing, books, sports, smartphones, laptops, tablets,
         accessories, menClothing, womenClothing, shoes, fiction, nonFiction, technical );
   }

   private Category createAndSaveCategory( String name, String description, Category parent ) {
      Category category = new Category();
      category.setName( name );
      category.setDescription( description );
      category.setParent( parent );
      return categoryRepository.save( category );
   }

   private List< ProductModel > createProductModels() {
      return new ArrayList<>();
//      log.info( "Creating product models..." );
//
//      // Electronics Product Models
//      ProductModel iphoneModel = createAndSaveProductModel(
//            "iPhone",
//            "IPHONE",
//            "Apple iPhone smartphone series"
//      );
//
//      ProductModel samsungModel = createAndSaveProductModel(
//            "Galaxy",
//            "GALAXY",
//            "Samsung Galaxy smartphone series"
//      );
//
//      ProductModel macbookModel = createAndSaveProductModel(
//            "MacBook",
//            "MACBOOK",
//            "Apple MacBook laptop series"
//      );
//
//      ProductModel dellModel = createAndSaveProductModel(
//            "Dell XPS",
//            "DELL_XPS",
//            "Dell XPS laptop series"
//      );
//
//      // Clothing Product Models
//      ProductModel tshirtModel = createAndSaveProductModel(
//            "T-Shirt",
//            "TSHIRT",
//            "Basic t-shirt design"
//      );
//
//      ProductModel jeansModel = createAndSaveProductModel(
//            "Jeans",
//            "JEANS",
//            "Denim jeans"
//      );
//
//      // Book Product Models
//      ProductModel programmingBookModel = createAndSaveProductModel(
//            "Programming Books",
//            "PROG_BOOK",
//            "Technical programming books"
//      );
//
//      // Create some model variants (parent-child relationships)
//      ProductModel iphone15Model = createAndSaveProductModel(
//            "iPhone 15",
//            "IPHONE15",
//            "iPhone 15 series",
//            iphoneModel
//      );
//
//      ProductModel iphone14Model = createAndSaveProductModel(
//            "iPhone 14",
//            "IPHONE14",
//            "iPhone 14 series",
//            iphoneModel
//      );
//
//      return List.of( iphoneModel, samsungModel, macbookModel, dellModel, tshirtModel,
//            jeansModel, programmingBookModel, iphone15Model, iphone14Model );
   }

   private ProductModel createAndSaveProductModel( String name, String reference, String description ) {
      return createAndSaveProductModel( name, reference, description, null );
   }

   private ProductModel createAndSaveProductModel( String name, String reference, String description, ProductModel parent ) {
      ProductModel model = new ProductModel();
//      model.setId( null );
//      model.setName( name );
//      model.setReference( reference );
//      model.setDescription( description );
//      model.setParentModel( parent );
//      return productModelRepository.save( model );
      return null;
   }

   private void createProducts( List< ProductModel > productModels ) {
      log.info( "Creating products..." );

      // Find specific models
      ProductModel iphoneModel = null;
//            productModels.stream()
//            .filter( pm -> "IPHONE15".equals( pm.getReference() ) )
//            .findFirst().orElse( productModels.get( 0 ) );

      ProductModel samsungModel = null;
//            productModels.stream()
//            .filter( pm -> "GALAXY".equals( pm.getReference() ) )
//            .findFirst().orElse( productModels.get( 1 ) );

      ProductModel macbookModel = null;
//            productModels.stream()
//            .filter( pm -> "MACBOOK".equals( pm.getReference() ) )
//            .findFirst().orElse( productModels.get( 2 ) );

      // iPhone Products
      createAndSaveProduct(
         "iPhone 15 Pro 128GB Space Black",
         "Latest iPhone with Pro features, 128GB storage in Space Black",
         "IPHONE15-PRO-128-BLACK",
         "iphone-15-pro-128gb-space-black",
         "Apple",
         new BigDecimal( "999.99" ),
         "USD",
         50, 0, 0,
         iphoneModel
      );

      createAndSaveProduct(
         "iPhone 15 Pro 256GB Natural Titanium",
         "Latest iPhone with Pro features, 256GB storage in Natural Titanium",
         "IPHONE15-PRO-256-TITANIUM",
         "iphone-15-pro-256gb-natural-titanium",
         "Apple",
         new BigDecimal( "1099.99" ),
         "USD",
         30, 0, 0,
         iphoneModel
      );

      // Samsung Products
      createAndSaveProduct(
         "Samsung Galaxy S24 Ultra 256GB Black",
         "Premium Samsung smartphone with S Pen, 256GB storage",
         "GALAXY-S24-ULTRA-256-BLACK",
         "samsung-galaxy-s24-ultra-256gb-black",
         "Samsung",
         new BigDecimal( "1199.99" ),
         "USD",
         25, 0, 0,
         samsungModel
      );

      // MacBook Products
      createAndSaveProduct(
         "MacBook Pro 14-inch M3 512GB Space Gray",
         "14-inch MacBook Pro with M3 chip, 512GB SSD, Space Gray",
         "MBP14-M3-512-GRAY",
         "macbook-pro-14-m3-512gb-space-gray",
         "Apple",
         new BigDecimal( "1999.99" ),
         "USD",
         15, 0, 0,
         macbookModel
      );

      createAndSaveProduct(
         "MacBook Air 13-inch M2 256GB Midnight",
         "13-inch MacBook Air with M2 chip, 256GB SSD, Midnight",
         "MBA13-M2-256-MIDNIGHT",
         "macbook-air-13-m2-256gb-midnight",
         "Apple",
         new BigDecimal( "1099.99" ),
         "USD",
         40, 0, 0,
         macbookModel
      );

      // Clothing Products
      ProductModel tshirtModel = productModels.stream()
//            .filter( pm -> "TSHIRT".equals( pm.getReference() ) )
         .findFirst().orElse( null );

      if ( tshirtModel != null ) {
         createAndSaveProduct(
            "Classic Cotton T-Shirt White - Medium",
            "100% cotton classic fit t-shirt in white, size medium",
            "TSHIRT-WHITE-M",
            "classic-cotton-tshirt-white-medium",
            "Generic",
            new BigDecimal( "19.99" ),
            "USD",
            100, 0, 0,
            tshirtModel
         );

         createAndSaveProduct(
            "Classic Cotton T-Shirt Black - Large",
            "100% cotton classic fit t-shirt in black, size large",
            "TSHIRT-BLACK-L",
            "classic-cotton-tshirt-black-large",
            "Generic",
            new BigDecimal( "19.99" ),
            "USD",
            80, 0, 0,
            tshirtModel
         );
      }

      // Book Products
      ProductModel programmingModel = productModels.stream()
//            .filter( pm -> "PROG_BOOK".equals( pm.getReference() ) )
         .findFirst().orElse( null );

      if ( programmingModel != null ) {
         createAndSaveProduct(
            "Clean Code: A Handbook of Agile Software Craftsmanship",
            "Essential book for software developers on writing clean, maintainable code",
            "BOOK-CLEAN-CODE",
            "clean-code-handbook-agile-software-craftsmanship",
            "Prentice Hall",
            new BigDecimal( "42.99" ),
            "USD",
            75, 0, 0,
            programmingModel
         );

         createAndSaveProduct(
            "Design Patterns: Elements of Reusable Object-Oriented Software",
            "Classic book on software design patterns by the Gang of Four",
            "BOOK-DESIGN-PATTERNS",
            "design-patterns-elements-reusable-oop-software",
            "Addison-Wesley",
            new BigDecimal( "54.99" ),
            "USD",
            45, 0, 0,
            programmingModel
         );
      }
   }

   private void createAndSaveProduct( String name, String description, String sku, String slug,
                                      String brand, BigDecimal price, String currency,
                                      int stock, int bought, int cart, ProductModel productModel ) {
      Product product = new Product();
      product.setName( name );
      product.setDescription( description );
      product.setSku( sku );
      product.setSlug( slug );
      product.setBrand( brand );
      product.setPrice( price );
      product.setCurrency( currency );
      product.setStock( stock );
      product.setBought( bought );
      product.setCart( cart );
//      product.setProductModel( productModel );

      productRepository.save( product );
   }

}

