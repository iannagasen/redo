package dev.agasen.core.product.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "products" )
public class ProductEntity {

   @Id
   @Column( columnDefinition = "uuid", updatable = false, nullable = false )
   private UUID id;

   private String name;
   private String description;
   private String sku;
   private String slug;
   private String brand;
   private BigDecimal price;
   private String currency;
   private Integer stockQuantity;

   @Convert( converter = MapToJsonbConverter.class )
   @Column( columnDefinition = "jsonb" )
   private Map< String, Object > attributesJson = new HashMap<>();

   private UUID categoryId;
   private Instant createdAt;
   private Instant updatedAt;
}