package dev.agasen.core.payment.application.write;

import dev.agasen.api.event.PaymentEvent;
import dev.agasen.api.payment.InitiatePaymentRequest;
import dev.agasen.api.payment.PaymentDetails;
import dev.agasen.core.payment.application.read.PaymentRetrievalService;
import dev.agasen.core.payment.domain.Payment;
import dev.agasen.core.payment.domain.PaymentRepository;
import dev.agasen.core.payment.domain.PaymentStatus;
import dev.agasen.core.payment.event.PaymentEventPublisher;
import dev.agasen.core.payment.gateway.GatewayPaymentRequest;
import dev.agasen.core.payment.gateway.GatewayPaymentResponse;
import dev.agasen.core.payment.gateway.PaymentGatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentCommandService {

   private final PaymentRepository paymentRepository;
   private final PaymentGatewayClient paymentGatewayClient;
   private final PaymentEventPublisher paymentEventPublisher;

   public PaymentDetails initiatePayment( String userId, InitiatePaymentRequest request ) {
      Payment payment = new Payment();
      payment.setOrderId( request.getOrderId() );
      payment.setUserId( userId );
      payment.setAmount( request.getAmount() );
      payment.setCurrency( request.getCurrency() );
      payment.setStatus( PaymentStatus.PENDING );
      Payment saved = paymentRepository.save( payment );

      GatewayPaymentRequest gatewayRequest = GatewayPaymentRequest.builder()
            .orderId( request.getOrderId() )
            .amount( request.getAmount() )
            .currency( request.getCurrency() )
            .cardNumber( request.getCardNumber() )
            .cardholderName( request.getCardholderName() )
            .expiryMonth( request.getExpiryMonth() )
            .expiryYear( request.getExpiryYear() )
            .cvv( request.getCvv() )
            .build();

      GatewayPaymentResponse gatewayResponse = paymentGatewayClient.processPayment( gatewayRequest );

      if ( gatewayResponse.isSuccess() ) {
         saved.setStatus( PaymentStatus.CAPTURED );
         saved.setGatewayRef( gatewayResponse.getGatewayRef() );
         log.info( "Payment CAPTURED for orderId={}, gatewayRef={}", request.getOrderId(), gatewayResponse.getGatewayRef() );
      } else {
         saved.setStatus( PaymentStatus.FAILED );
         saved.setFailureReason( gatewayResponse.getFailureReason() );
         log.info( "Payment FAILED for orderId={}, reason={}", request.getOrderId(), gatewayResponse.getFailureReason() );
      }

      Payment updated = paymentRepository.save( saved );

      PaymentEvent event = new PaymentEvent(
            updated.getOrderId(),
            updated.getId(),
            updated.getUserId(),
            updated.getAmount(),
            updated.getStatus().name(),
            updated.getFailureReason()
      );
      paymentEventPublisher.publish( event );

      return PaymentRetrievalService.toPaymentDetails( updated );
   }
}
