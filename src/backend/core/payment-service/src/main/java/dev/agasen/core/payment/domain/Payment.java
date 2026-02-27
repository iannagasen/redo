package dev.agasen.core.payment.domain;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table( name = "payments" )
@Getter
@Setter
public class Payment extends BaseEntity {

   @Column( name = "order_id", nullable = false )
   private Long orderId;

   @Column( name = "user_id", nullable = false )
   private String userId;

   @Column( nullable = false, precision = 19, scale = 2 )
   private BigDecimal amount;

   @Column( length = 10 )
   private String currency;

   @Enumerated( EnumType.STRING )
   @Column( nullable = false )
   private PaymentStatus status = PaymentStatus.PENDING;

   @Column( name = "gateway_ref" )
   private String gatewayRef;

   @Column( name = "failure_reason" )
   private String failureReason;
}
