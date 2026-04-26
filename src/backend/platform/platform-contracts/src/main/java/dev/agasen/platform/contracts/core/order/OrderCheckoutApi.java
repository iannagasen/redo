package dev.agasen.platform.contracts.core.order;

import dev.agasen.platform.contracts.core.order.read.OrderDetails;
import dev.agasen.platform.contracts.core.order.write.CheckoutRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/// HTTP client interface for the checkout endpoint.
/// Used by storefront-bff to call order-service.
/// Returns an OrderDetails in PENDING status — the saga continues asynchronously.
/// The caller should poll GET /orders/{id} until status is CONFIRMED or CANCELLED.
@HttpExchange( "/api/v1/orders/checkout" )
public interface OrderCheckoutApi {

   @PostExchange
   OrderDetails checkout( @RequestBody @Valid CheckoutRequest request );

}
