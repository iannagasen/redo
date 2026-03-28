package dev.agasen.core.order.fixtures;

import dev.agasen.api.payment.read.PaymentDetails;

import java.math.BigDecimal;

public class PaymentDetailsTestBuilder {

   public static PaymentDetails capturedPayment( Long orderId, BigDecimal amount ) {
      var payment = new PaymentDetails();
      payment.setId( 100L );
      payment.setOrderId( orderId );
      payment.setAmount( amount );
      payment.setCurrency( "USD" );
      payment.setStatus( "CAPTURED" );
      return payment;
   }
}
