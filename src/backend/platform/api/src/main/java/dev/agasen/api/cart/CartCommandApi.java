package dev.agasen.api.cart;

import dev.agasen.api.cart.read.CartDetails;
import dev.agasen.api.cart.write.AddCartItemRequest;
import dev.agasen.api.cart.write.UpdateCartItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange( "/api/v1/cart" )
public interface CartCommandApi {

   @PostExchange( "/items" )
   CartDetails addItem( @RequestBody @Valid AddCartItemRequest req );

   @PutExchange( "/items/{productId}" )
   CartDetails updateQuantity( @PathVariable Long productId, @RequestBody @Valid UpdateCartItemRequest req );

   @DeleteExchange( "/items/{productId}" )
   ResponseEntity< Void > removeItem( @PathVariable Long productId );

   @DeleteExchange
   ResponseEntity< Void > clearCart();
}
