package dev.agasen.core.product.domain.persistence.entity;

import dev.agasen.core.product.domain.product.Product;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@ToString
//@Entity
//@Table( name = "product_models" )
public class ProductModel {

   @Id
   @GeneratedValue( strategy = GenerationType.IDENTITY )
   private Long id;

   @Column( nullable = false )
   private String name;

   @Column( nullable = false, unique = true )
   private String reference;

   @Column( nullable = false )
   private String description;

   @ManyToOne( fetch = FetchType.LAZY )
   @JoinColumn( name = "parent_model_id" )
   private ProductModel parentModel;

   @OneToMany( mappedBy = "parentModel", fetch = FetchType.LAZY )
   private List< ProductModel > childModels = new ArrayList<>();

//   // Bidirectional Mapping
//   @OneToMany( mappedBy = "productModel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
//   private List< AttributeSet > attributeSets = new ArrayList<>();

   // Bidirectional Mapping
   @OneToMany( mappedBy = "productModel", fetch = FetchType.LAZY )
   private List< Product > products = new ArrayList<>();

}
