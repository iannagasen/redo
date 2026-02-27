package dev.agasen.core.payment.gateway;

/**
 * Facade interface for payment gateway integration.
 * Implementations can be swapped (Mock, Stripe, PayPal) without changing business logic.
 */
public interface PaymentGatewayClient {
   GatewayPaymentResponse processPayment( GatewayPaymentRequest request );
}
