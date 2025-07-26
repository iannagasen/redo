package dev.agasen.core.product.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "attributes" )
public class Attribute {

   @Id
   @Column( columnDefinition = "uuid", updatable = false, nullable = false )
   private UUID id;

   @ManyToOne( fetch = FetchType.LAZY, optional = false )
   @JoinColumn( name = "product_id", nullable = false )
   private Product product;

   @ManyToOne( fetch = FetchType.LAZY, optional = false )
   @JoinColumn( name = "attribute_set_id", nullable = false )
   private AttributeSet attributeSet;

   @Enumerated( EnumType.STRING )
   @Column( name = "data_type", nullable = false )
   private DataType dataType;

   @Column( columnDefinition = "TEXT" )
   private String value;

   private String stringValue;
   private Integer integerValue;
   private BigDecimal decimalValue;
   private Boolean booleanValue;
   private LocalDate dateValue;

   public void setValue( Object obj ) {
      switch ( obj ) {
         case String s when dataType == DataType.STRING -> stringValue = s;
         case Integer i when dataType == DataType.INTEGER -> integerValue = i;
         case BigDecimal bd when dataType == DataType.DECIMAL -> decimalValue = bd;
         case Boolean bt when dataType == DataType.BOOLEAN -> booleanValue = bt;
         case LocalDate ld when dataType == DataType.DATE -> dateValue = ld;
         default -> throw new IllegalStateException( "Unexpected value: " + obj + " for dataType: " + dataType );
      }
      value = obj.toString();
   }

   public Object getTypedValue() {
      return switch ( dataType ) {
         case STRING -> stringValue;
         case INTEGER -> integerValue;
         case DECIMAL -> decimalValue;
         case BOOLEAN -> booleanValue;
         case DATE -> dateValue;
      };
   }
}
