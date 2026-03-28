package dev.agasen.core.payment.repository;

import dev.agasen.core.payment.repository.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository< Payment, Long > {

   List< Payment > findByUserIdOrderByCreatedAtDesc( String userId );

   Optional< Payment > findByOrderId( Long orderId );

}
