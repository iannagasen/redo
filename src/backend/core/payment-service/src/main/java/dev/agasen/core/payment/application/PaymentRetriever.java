package dev.agasen.core.payment.application;

import dev.agasen.api.payment.read.PaymentDetails;
import dev.agasen.common.exceptions.Exceptions;
import dev.agasen.core.payment.repository.entity.Payment;
import dev.agasen.core.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true )
public class PaymentRetriever {

   private final PaymentRepository paymentRepository;

   public List< PaymentDetails > getPayments( String userId ) {
      return paymentRepository.findByUserIdOrderByCreatedAtDesc( userId )
         .stream()
         .map( PaymentRetriever::toPaymentDetails )
         .toList();
   }

   public PaymentDetails getPaymentById( Long id ) {
      Payment payment = paymentRepository.findById( id )
         .orElseThrow( Exceptions.notFound( "Payment", id ) );
      return toPaymentDetails( payment );
   }

   public PaymentDetails getPaymentByOrderId( Long orderId ) {
      return paymentRepository.findByOrderId( orderId )
         .map( PaymentRetriever::toPaymentDetails )
         .orElseThrow( Exceptions.notFound( "Payment", orderId ) );
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
