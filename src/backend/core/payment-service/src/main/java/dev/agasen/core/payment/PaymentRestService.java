package dev.agasen.core.payment;

import dev.agasen.api.payment.InitiatePaymentRequest;
import dev.agasen.api.payment.PaymentApi;
import dev.agasen.api.payment.PaymentDetails;
import dev.agasen.core.payment.application.read.PaymentRetrievalService;
import dev.agasen.core.payment.application.write.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentRestService implements PaymentApi {

   private final PaymentRetrievalService paymentRetrievalService;
   private final PaymentCommandService paymentCommandService;

   public List< PaymentDetails > getPayments() {
      return paymentRetrievalService.getPayments( currentUserId() );
   }

   public PaymentDetails getPaymentByOrderId( Long orderId ) {
      return paymentRetrievalService.getPaymentByOrderId( orderId );
   }

   public PaymentDetails getPaymentById( Long id ) {
      return paymentRetrievalService.getPaymentById( id );
   }

   @ResponseStatus( HttpStatus.CREATED )
   public PaymentDetails initiatePayment( InitiatePaymentRequest request ) {
      return paymentCommandService.initiatePayment( currentUserId(), request );
   }

   private String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
