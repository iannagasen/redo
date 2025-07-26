package dev.agasen.core.product.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table( name = "product_models" )
public class ProductModel {

   @Id
   @Column( columnDefinition = "uuid", updatable = false, nullable = false )
   private UUID id;

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

   // Bidirectional Mapping
   @OneToMany( mappedBy = "productModel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
   private List< AttributeSet > attributeSets = new ArrayList<>();

   // Bidirectional Mapping
   @OneToMany( mappedBy = "productModel", fetch = FetchType.LAZY )
   private List< Product > products = new ArrayList<>();

}
