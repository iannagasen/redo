package dev.agasen.core.payment.application.read;

import dev.agasen.api.payment.PaymentDetails;
import dev.agasen.common.exceptions.Exceptions;
import dev.agasen.core.payment.domain.Payment;
import dev.agasen.core.payment.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true )
public class PaymentRetrievalService {

   private final PaymentRepository paymentRepository;

   public List<PaymentDetails> getPayments( String userId ) {
      return paymentRepository.findByUserIdOrderByCreatedAtDesc( userId )
            .stream()
            .map( PaymentRetrievalService::toPaymentDetails )
            .toList();
   }

   public PaymentDetails getPaymentById( Long id ) {
      Payment payment = paymentRepository.findById( id )
            .orElseThrow( Exceptions.notFound( "Payment", id ) );
      return toPaymentDetails( payment );
   }

   public static PaymentDetails toPaymentDetails( Payment payment ) {
      PaymentDetails details = new PaymentDetails();
      details.setId( payment.getId() );
      details.setOrderId( payment.getOrderId() );
      details.setUserId( payment.getUserId() );
      details.setAmount( payment.getAmount() );
      details.setCurrency( payment.getCurrency() );
      details.setStatus( payment.getStatus().name() );
      details.setCreatedAt( payment.getCreatedAt() );
      return details;
   }
}
