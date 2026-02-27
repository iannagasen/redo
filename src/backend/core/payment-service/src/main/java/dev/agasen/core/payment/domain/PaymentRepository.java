package dev.agasen.core.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
   List<Payment> findByUserIdOrderByCreatedAtDesc( String userId );
}
