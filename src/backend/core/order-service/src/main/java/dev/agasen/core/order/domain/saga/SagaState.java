package dev.agasen.core.order.domain.saga;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One row per saga instance.
 *
 * <p>status lifecycle: RUNNING → COMPLETED | FAILED | STUCK
 * STUCK is not set here — it is detected by a scheduled job
 * polling {@code WHERE status = 'RUNNING' AND updated_at < NOW() - INTERVAL '10 minutes'}.
 *
 * <p>{@code @Version} prevents two concurrent event-handler threads from
 * overwriting each other's participant updates (optimistic locking).
 */
@Entity
@Table( name = "saga_state" )
@EntityListeners( AuditingEntityListener.class )
@Getter
@Setter
public class SagaState {

   @Id
   @Column( name = "saga_id" )
   private UUID sagaId;

   @Column( name = "saga_type", nullable = false )
   private String sagaType;

   /**
    * FK to orders.id — one active saga per order at a time.
    */
   @Column( name = "order_id", nullable = false, unique = true )
   private Long orderId;

   @Column( nullable = false )
   private String status;

   @Version
   private Long version;

   @CreatedDate
   @Column( name = "created_at", updatable = false )
   private Instant createdAt;

   @LastModifiedDate
   @Column( name = "updated_at" )
   private Instant updatedAt;

   @OneToMany( mappedBy = "sagaState", cascade = CascadeType.ALL, orphanRemoval = true )
   private List< SagaParticipant > participants = new ArrayList<>();

   public void updateParticipantStatus( String paticipantName, String status ) {
      getParticipants().stream()
         .filter( p -> p.getParticipant().equals( paticipantName ) )
         .findFirst()
         .ifPresent( p -> p.setStatus( status ) );
   }

   public boolean areAllParticipantsSucessful() {
      return getParticipants().stream()
         .map( SagaParticipant::getStatus )
         .allMatch( "SUCCESSFUL"::equals );
   }

   public List< SagaParticipant > getPendingParticipants() {
      return getParticipants().stream()
         .filter( p -> p.getStatus().equals( "PENDING" ) )
         .toList();
   }

   public boolean hasPendingParticipants() {
      return !getPendingParticipants().isEmpty();
   }

}
