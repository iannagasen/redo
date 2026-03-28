package dev.agasen.core.payment.repository.entity;

import dev.agasen.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
   name = "idempotency_records",
   uniqueConstraints = {
      @UniqueConstraint(
         name = "uk_idempotency_key_user",
         columnNames = { "idempotency_key", "user_id" }
      )
   }
)
public class IdempotencyRecord extends BaseEntity {

   @Column( name = "idempotency_key", nullable = false )
   private UUID idempotencyKey;

   @Column( name = "user_id", nullable = false )
   private String userId;

   @Column( name = "response_body", nullable = false, columnDefinition = "TEXT" )
   private String responseBody;

}
