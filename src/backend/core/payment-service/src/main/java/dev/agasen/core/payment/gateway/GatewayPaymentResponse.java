package dev.agasen.core.payment.gateway;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GatewayPaymentResponse {
   boolean success;
   String gatewayRef;
   String failureReason;
}
