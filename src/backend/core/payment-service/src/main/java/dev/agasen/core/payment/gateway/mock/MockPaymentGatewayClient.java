package dev.agasen.core.payment.gateway.mock;

import dev.agasen.core.payment.gateway.GatewayPaymentRequest;
import dev.agasen.core.payment.gateway.GatewayPaymentResponse;
import dev.agasen.core.payment.gateway.PaymentGatewayClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock implementation of PaymentGatewayClient.
 * Card "4000000000000002" simulates a declined card; all others succeed.
 */
@Component
@Primary
public class MockPaymentGatewayClient implements PaymentGatewayClient {

   private static final String DECLINED_CARD = "4000000000000002";

   @Override
   public GatewayPaymentResponse processPayment( GatewayPaymentRequest request ) {
      String normalizedCard = request.getCardNumber().replaceAll( "\\s+", "" );

      if ( DECLINED_CARD.equals( normalizedCard ) ) {
         return GatewayPaymentResponse.builder()
               .success( false )
               .failureReason( "Card declined" )
               .build();
      }

      return GatewayPaymentResponse.builder()
            .success( true )
            .gatewayRef( "mock-" + UUID.randomUUID() )
            .build();
   }
}
