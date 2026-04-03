package dev.agasen.core.order.domain.saga;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * One row per participant per saga instance.
 *
 * <p>participant examples: "PAYMENT", "CART", "INVENTORY"
 *
 * <p>status lifecycle: PENDING → SUCCESS | FAILED
 *
 * <p>{@code required = true}  → this participant must succeed for the order to be confirmed.
 * {@code required = false} → best-effort; failure is logged but does not cancel the order.
 */
@Entity
@Table( name = "saga_participant" )
@Getter
@Setter
public class SagaParticipant extends BaseEntity {

   @ManyToOne( fetch = FetchType.LAZY )
   @JoinColumn( name = "saga_id", nullable = false )
   private SagaState sagaState;

   @Column( nullable = false )
   private String participant;

   /**
    * If true, this participant must reach SUCCESS before the order can be confirmed.
    */
   @Column( nullable = false )
   private boolean required;

   @Column( nullable = false )
   private String status = "PENDING";

   public String toString() {
      return "[id=%d, participant=%s, status=%s]"
         .formatted( getId(), getParticipant(), getStatus() );
   }
}
