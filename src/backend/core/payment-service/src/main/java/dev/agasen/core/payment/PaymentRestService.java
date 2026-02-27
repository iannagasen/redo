package dev.agasen.core.payment;

import dev.agasen.api.payment.InitiatePaymentRequest;
import dev.agasen.api.payment.PaymentDetails;
import dev.agasen.core.payment.application.read.PaymentRetrievalService;
import dev.agasen.core.payment.application.write.PaymentCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( "/api/v1/payments" )
@RequiredArgsConstructor
public class PaymentRestService {

   private final PaymentRetrievalService paymentRetrievalService;
   private final PaymentCommandService paymentCommandService;

   @GetMapping
   public List<PaymentDetails> getPayments() {
      return paymentRetrievalService.getPayments( currentUserId() );
   }

   @GetMapping( "/{id}" )
   public PaymentDetails getPaymentById( @PathVariable Long id ) {
      return paymentRetrievalService.getPaymentById( id );
   }

   @PostMapping
   @ResponseStatus( HttpStatus.CREATED )
   public PaymentDetails initiatePayment( @RequestBody @Valid InitiatePaymentRequest request ) {
      return paymentCommandService.initiatePayment( currentUserId(), request );
   }

   private String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
