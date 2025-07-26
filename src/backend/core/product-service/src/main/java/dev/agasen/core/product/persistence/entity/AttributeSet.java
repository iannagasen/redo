package dev.agasen.core.product.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
      name = "attribute_set",
      uniqueConstraints = @UniqueConstraint( columnNames = { "product_model_id", "attribute_model_id" } )
)
public class AttributeSet {

   @Id
   @Column( columnDefinition = "uuid", updatable = false, nullable = false )
   private UUID id;

   @ManyToOne( fetch = FetchType.LAZY )
   @JoinColumn( name = "product_model_id" )
   private ProductModel productModel;

   @ManyToOne( fetch = FetchType.LAZY )
   @JoinColumn( name = "attribute_model_id" )
   private AttributeModel attributeModel;

   @Column( nullable = false )
   private boolean required = false;

   private String displayName;  // Override display name for this model

   // Bidirectional relationship with Attribute
   @OneToMany( mappedBy = "attributeSet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
   private List< Attribute > attributes = new ArrayList<>();
}
