package dev.agasen.platform.contracts.core.payment;

import dev.agasen.platform.contracts.core.payment.read.PaymentDetails;
import dev.agasen.platform.contracts.core.payment.write.InitiatePaymentRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.UUID;

@HttpExchange( "/api/v1/payments" )
public interface PaymentApi {

   @GetExchange
   List< PaymentDetails > getPayments();

   @GetExchange( "/order/{orderId}" )
   PaymentDetails getPaymentByOrderId( @PathVariable Long orderId );

   @GetExchange( "/{id}" )
   PaymentDetails getPaymentById( @PathVariable Long id );

   @PostExchange
   PaymentDetails initiatePayment(
      @RequestBody @Valid InitiatePaymentRequest request,
      @RequestHeader( value = "Idempotency-Key", required = true ) UUID idempotencyKey
   );
}
