package dev.agasen.core.payment;

import dev.agasen.platform.contracts.core.payment.write.InitiatePaymentRequest;
import dev.agasen.platform.contracts.core.payment.PaymentApi;
import dev.agasen.platform.contracts.core.payment.read.PaymentDetails;
import dev.agasen.core.payment.application.IdempotencyStore;
import dev.agasen.core.payment.application.PaymentRetriever;
import dev.agasen.core.payment.application.PaymentInitiator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentRestService implements PaymentApi {

   private final PaymentRetriever paymentRetriever;
   private final PaymentInitiator paymentInitiator;
   private final IdempotencyStore idempotencyStore;

   public List< PaymentDetails > getPayments() {
      return paymentRetriever.getPayments( currentUserId() );
   }

   public PaymentDetails getPaymentByOrderId( Long orderId ) {
      return paymentRetriever.getPaymentByOrderId( orderId );
   }

   public PaymentDetails getPaymentById( Long id ) {
      return paymentRetriever.getPaymentById( id );
   }

   @ResponseStatus( HttpStatus.CREATED )
   public PaymentDetails initiatePayment( InitiatePaymentRequest request, UUID idempotencyKey ) {
      var paymentIdempotencyCheck = idempotencyStore.find( idempotencyKey, currentUserId() );
      if ( paymentIdempotencyCheck.isEmpty() ) {
         var paymentDetails = paymentInitiator.initiatePayment( currentUserId(), request );
         idempotencyStore.save( idempotencyKey, currentUserId(), paymentDetails );
         return paymentDetails;
      } else {
         return paymentIdempotencyCheck.get();
      }
   }

   private String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
