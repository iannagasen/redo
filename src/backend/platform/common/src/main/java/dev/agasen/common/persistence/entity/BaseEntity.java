package dev.agasen.common.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners( AuditingEntityListener.class )
public abstract class BaseEntity {

   @Id
   @GeneratedValue( strategy = GenerationType.IDENTITY )
   private Long id;

   @CreatedDate
   @Column( updatable = false )
   protected Instant createdAt;

   @LastModifiedDate
   protected Instant updatedAt;

   public Long getId() {
      return id;
   }

   public Instant getCreatedAt() {
      return createdAt;
   }

   public Instant getUpdatedAt() {
      return updatedAt;
   }
}
