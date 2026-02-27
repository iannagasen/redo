package dev.agasen.core.payment.gateway;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class GatewayPaymentRequest {
   Long orderId;
   BigDecimal amount;
   String currency;
   String cardNumber;
   String cardholderName;
   Integer expiryMonth;
   Integer expiryYear;
   String cvv;
}
