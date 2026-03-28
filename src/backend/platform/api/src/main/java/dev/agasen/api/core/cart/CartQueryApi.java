package dev.agasen.api.core.cart;

import dev.agasen.api.core.cart.read.CartDetails;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange( "/api/v1/cart" )
public interface CartQueryApi {

   @GetExchange
   CartDetails getCart();
}
