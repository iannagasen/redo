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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "attribute_models" )
public class AttributeModel {

   @Id
   @Column( columnDefinition = "uuid", updatable = false, nullable = false )
   private UUID id;

   @Column( nullable = false )
   private String name;

   @Column( nullable = false, updatable = false )
   private String reference;

   @Enumerated( EnumType.STRING )
   private DataType dataType;

   @OneToMany( mappedBy = "attributeModel", fetch = FetchType.LAZY )
   private List< AttributeSet > attributeSets = new ArrayList<>();

}
