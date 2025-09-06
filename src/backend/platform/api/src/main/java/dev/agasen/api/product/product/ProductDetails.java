package dev.agasen.api.product.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDetails {
   private UUID id;
   private String name;
   private String description;
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