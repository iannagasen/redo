package dev.agasen.core.payment.repository;

import dev.agasen.core.payment.repository.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository extends JpaRepository< IdempotencyRecord, Long > {

   Optional< IdempotencyRecord > findByIdempotencyKeyAndUserId( UUID idempotencyKey, String userId );

}
