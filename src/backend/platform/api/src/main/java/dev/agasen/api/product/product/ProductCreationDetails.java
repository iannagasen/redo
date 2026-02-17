package dev.agasen.api.product.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductCreationDetails {
   private String name;
   private String description;
   private String sku;
   private String slug;
   private String brand;
   private BigDecimal price;
   private String currency;
   private int stock;
   private Map< String, Object > attributesJson;
}
