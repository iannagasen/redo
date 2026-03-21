package dev.agasen.api.payment;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange( "/api/v1/payments" )
public interface PaymentApi {

   @GetExchange
   List< PaymentDetails > getPayments();

   @GetExchange( "/order/{orderId}" )
   PaymentDetails getPaymentByOrderId( @PathVariable Long orderId );

   @GetExchange( "/{id}" )
   PaymentDetails getPaymentById( @PathVariable Long id );

   @PostExchange
   PaymentDetails initiatePayment( @RequestBody @Valid InitiatePaymentRequest request );
}
