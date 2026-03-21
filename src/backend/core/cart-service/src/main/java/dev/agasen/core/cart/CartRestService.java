package dev.agasen.core.cart;

import dev.agasen.api.cart.AddToCartRequest;
import dev.agasen.api.cart.CartApi;
import dev.agasen.api.cart.CartDetails;
import dev.agasen.api.cart.UpdateCartItemRequest;
import dev.agasen.core.cart.application.read.CartRetrievalService;
import dev.agasen.core.cart.application.write.CartCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartRestService implements CartApi {

   private final CartRetrievalService retrievalService;
   private final CartCommandService commandService;

   public CartDetails getCart() {
      return retrievalService.getCart( userId() );
   }

   public CartDetails addItem( AddToCartRequest req ) {
      return commandService.addItem( userId(), req );
   }

   public CartDetails updateQuantity( Long productId, UpdateCartItemRequest req ) {
      return commandService.updateItemQuantity( userId(), productId, req.getQuantity() );
   }

   public ResponseEntity< Void > removeItem( Long productId ) {
      commandService.removeItem( userId(), productId );
      return ResponseEntity.noContent().build();
   }

   public ResponseEntity< Void > clearCart() {
      commandService.clearCart( userId() );
      return ResponseEntity.noContent().build();
   }

   private String userId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
