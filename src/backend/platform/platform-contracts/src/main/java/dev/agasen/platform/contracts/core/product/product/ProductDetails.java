package dev.agasen.platform.contracts.core.product.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDetails {
   private Long id;
   private String name;
   private String description;
   private String imageUrl;
   private String sku;
   private String slug;
   private String brand;
   private BigDecimal price;
   private String currency;
   private int stock;
   private int bought;
   private int cart;
//      Map< String, Object > attributesJson
}