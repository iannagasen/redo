package dev.agasen.core.order.domain;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table( name = "order_items" )
@Getter
@Setter
public class OrderItem extends BaseEntity {
   @ManyToOne( fetch = FetchType.LAZY )
   @JoinColumn( name = "order_id", nullable = false )
   private Order order;

   @Column( name = "product_id", nullable = false )
   private Long productId;

   @Column( name = "product_name", nullable = false )
   private String productName;

   private String brand;

   @Column( nullable = false, precision = 19, scale = 2 )
   private BigDecimal price;

   private String currency;

   @Column( nullable = false )
   private int quantity;

   @Column( name = "line_total", nullable = false, precision = 19, scale = 2 )
   private BigDecimal lineTotal;
}
