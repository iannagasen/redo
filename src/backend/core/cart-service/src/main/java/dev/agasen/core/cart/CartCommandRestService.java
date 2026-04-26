package dev.agasen.core.cart;

import dev.agasen.platform.contracts.core.cart.read.CartDetails;
import dev.agasen.platform.contracts.core.cart.write.AddCartItemRequest;
import dev.agasen.platform.contracts.core.cart.write.UpdateCartItemRequest;
import dev.agasen.core.cart.application.write.CartCommandService;
import dev.agasen.platform.contracts.core.cart.CartCommandApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartCommandRestService implements CartCommandApi {

   private final CartCommandService commandService;

   @Override
   public CartDetails addItem( AddCartItemRequest req ) {
      return commandService.addItem( userId(), req );
   }

   @Override
   public CartDetails updateQuantity( Long productId, UpdateCartItemRequest req ) {
      return commandService.updateItemQuantity( userId(), productId, req.getQuantity() );
   }

   @Override
   public ResponseEntity< Void > removeItem( Long productId ) {
      commandService.removeItem( userId(), productId );
      return ResponseEntity.noContent().build();
   }

   @Override
   public ResponseEntity< Void > clearCart() {
      commandService.clearCart( userId() );
      return ResponseEntity.noContent().build();
   }

   private String userId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
