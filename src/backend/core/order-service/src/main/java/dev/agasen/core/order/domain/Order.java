package dev.agasen.core.order.domain;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table( name = "orders" )
@Getter
@Setter
public class Order extends BaseEntity {

   @Column( name = "user_id", nullable = false )
   private String userId;

   @Enumerated( EnumType.STRING )
   @Column( nullable = false )
   private OrderStatus status = OrderStatus.PENDING;

   @Column( nullable = false, precision = 19, scale = 2 )
   private BigDecimal total;

   @OneToMany( mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true )
   private List< OrderItem > items = new ArrayList<>();
}
