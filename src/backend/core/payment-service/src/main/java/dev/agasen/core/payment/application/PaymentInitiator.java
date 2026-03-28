package dev.agasen.core.payment.application;

import dev.agasen.api.event.PaymentEvent;
import dev.agasen.api.payment.write.InitiatePaymentRequest;
import dev.agasen.api.payment.read.PaymentDetails;
import dev.agasen.common.http.exceptions.BadRequestException;
import dev.agasen.core.payment.repository.entity.Payment;
import dev.agasen.core.payment.repository.PaymentRepository;
import dev.agasen.core.payment.repository.entity.PaymentStatus;
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
public class PaymentInitiator {

   private final PaymentRepository paymentRepository;
   private final PaymentGatewayClient paymentGatewayClient;
   private final PaymentEventPublisher paymentEventPublisher;

   public PaymentDetails initiatePayment( String userId, InitiatePaymentRequest request ) {
      if ( paymentAlreadyExists( request.getOrderId() ) ) {
         throw new BadRequestException( "Payment already existed for order " + request.getOrderId() );
      }

      Payment payment = savePaymentAsPending( userId, request );

      GatewayPaymentResponse gatewayResponse = paymentGatewayClient.processPayment( toGatewayRequest( request ) );

      if ( gatewayResponse.isSuccess() ) {
         updatePaymentToCaptured( payment, gatewayResponse.getGatewayRef() );
      } else {
         updatePaymentToFailed( payment, gatewayResponse.getFailureReason() );
      }

      Payment saved = paymentRepository.save( payment );

      paymentEventPublisher.publish( toPaymentEvent( saved ) );

      return PaymentRetriever.toPaymentDetails( saved );
   }

   private void updatePaymentToCaptured( Payment payment, String gatewayRef ) {
      payment.setStatus( PaymentStatus.CAPTURED );
      payment.setGatewayRef( gatewayRef );
      log.info( "Payment CAPTURED for orderId={}, gatewayRef={}", payment.getOrderId(), gatewayRef );
   }

   private void updatePaymentToFailed( Payment payment, String failureReason ) {
      payment.setStatus( PaymentStatus.FAILED );
      payment.setFailureReason( failureReason );
      log.info( "Payment FAILED for orderId={}, reason={}", payment.getOrderId(), failureReason );
   }

   private boolean paymentAlreadyExists( Long orderId ) {
      return paymentRepository.findByOrderId( orderId )
         .filter( p -> p.getStatus() != PaymentStatus.FAILED )
         .isPresent();
   }

   private Payment savePaymentAsPending( String userId, InitiatePaymentRequest request ) {
      Payment payment = new Payment();
      payment.setOrderId( request.getOrderId() );
      payment.setUserId( userId );
      payment.setAmount( request.getAmount() );
      payment.setCurrency( request.getCurrency() );
      payment.setStatus( PaymentStatus.PENDING );
      return paymentRepository.save( payment );
   }

   private GatewayPaymentRequest toGatewayRequest( InitiatePaymentRequest request ) {
      return GatewayPaymentRequest.builder()
         .orderId( request.getOrderId() )
         .amount( request.getAmount() )
         .currency( request.getCurrency() )
         .cardNumber( request.getCardNumber() )
         .cardholderName( request.getCardholderName() )
         .expiryMonth( request.getExpiryMonth() )
         .expiryYear( request.getExpiryYear() )
         .cvv( request.getCvv() )
         .build();
   }

   private PaymentEvent toPaymentEvent( Payment payment ) {
      return new PaymentEvent(
         payment.getOrderId(),
         payment.getId(),
         payment.getUserId(),
         payment.getAmount(),
         payment.getStatus().name(),
         payment.getFailureReason()
      );
   }
}
