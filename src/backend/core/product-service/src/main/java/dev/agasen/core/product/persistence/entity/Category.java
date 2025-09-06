package dev.agasen.core.product.persistence.entity;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table( name = "categories" )
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category extends BaseEntity {

   private String name;
   private String description;

   @ManyToOne( fetch = FetchType.LAZY )
   private Category parent;

   @OneToMany( mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true )
   private List< Category > categories;


}
