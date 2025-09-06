package dev.agasen.core.product.persistence.entity;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "products" )
public class Product extends BaseEntity {

   @Column( nullable = false )
   private String name;

   @Column( columnDefinition = "TEXT" )
   private String description;

   @Column( nullable = false )
   private String sku;

   @Column( nullable = false )
   private String slug;

   private String brand;

   @Column( precision = 19, scale = 2 )
   private BigDecimal price;
   private String currency;

   private int stock = 0;
   private int bought = 0;
   private int cart = 0;

   @ManyToOne( fetch = FetchType.LAZY, optional = false )
   @JoinColumn( name = "product_model_id", nullable = false )
   private ProductModel productModel;

   // Product's actual attribute values
   @OneToMany( mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
   private List< Attribute > attributes = new ArrayList<>();

}