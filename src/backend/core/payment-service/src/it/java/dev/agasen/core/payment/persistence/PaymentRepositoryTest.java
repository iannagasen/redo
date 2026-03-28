package dev.agasen.core.payment.persistence;

import dev.agasen.core.payment.repository.entity.Payment;
import dev.agasen.core.payment.repository.PaymentRepository;
import dev.agasen.core.payment.repository.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class PaymentRepositoryTest {

   @Autowired PaymentRepository paymentRepository;

   @Test
   void givenTwoPaymentWithSimilarOrderId_whenInserted_ThenError() {
      Payment p1 = createPaymentWithOrderId( 1L );
      paymentRepository.saveAndFlush( p1 );

      Payment p2 = createPaymentWithOrderId( 1L );

      assertThrows( Exception.class, () -> {
         paymentRepository.saveAndFlush( p2 );
      } );
   }


   private Payment createPaymentWithOrderId( Long orderId ) {
      Payment p = new Payment();
      p.setOrderId( orderId );
      p.setUserId( "user-123" );
      p.setAmount( new BigDecimal( "100.00" ) );
      p.setCurrency( "USD" );
      p.setStatus( PaymentStatus.PENDING );
      p.setGatewayRef( "gw-ref-" + orderId );
      p.setFailureReason( null );
      return p;
   }
}
