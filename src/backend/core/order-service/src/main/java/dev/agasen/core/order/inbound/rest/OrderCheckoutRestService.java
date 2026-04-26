package dev.agasen.core.order.inbound.rest;

import dev.agasen.platform.contracts.core.order.OrderCheckoutApi;
import dev.agasen.platform.contracts.core.order.read.OrderDetails;
import dev.agasen.platform.contracts.core.order.write.CheckoutRequest;
import dev.agasen.core.order.application.write.OrderCheckoutSagaInitiator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderCheckoutRestService implements OrderCheckoutApi {

   private final OrderCheckoutSagaInitiator orderCheckoutSagaInitiator;

   @ResponseStatus( HttpStatus.CREATED )
   public OrderDetails checkout( CheckoutRequest request ) {
      return orderCheckoutSagaInitiator.checkout( request );
   }

}
