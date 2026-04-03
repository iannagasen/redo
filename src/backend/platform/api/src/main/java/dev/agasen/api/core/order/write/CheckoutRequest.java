package dev.agasen.api.core.order.write;

import dev.agasen.api.core.payment.write.InitiatePaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/// Request payload for the checkout endpoint.
/// Combines order items and payment details into a single request,
/// representing the full intent to purchase.
///
/// ## Note
/// Card details here are simplified for learning purposes.
/// In production, cards should be tokenized client-side (e.g., Stripe.js)
/// before reaching the backend — raw card numbers should never travel over the wire.
public record CheckoutRequest(

   @NotNull CreateOrderRequest orderDetails,
   @NotNull InitiatePaymentRequest paymentDetails

) {
}
