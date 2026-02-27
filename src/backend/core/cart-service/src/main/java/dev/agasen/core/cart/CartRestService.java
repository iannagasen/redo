package dev.agasen.core.cart;

import dev.agasen.api.cart.AddToCartRequest;
import dev.agasen.api.cart.CartDetails;
import dev.agasen.api.cart.UpdateCartItemRequest;
import dev.agasen.core.cart.application.read.CartRetrievalService;
import dev.agasen.core.cart.application.write.CartCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "/api/v1/cart" )
@RequiredArgsConstructor
public class CartRestService {

   private final CartRetrievalService retrievalService;
   private final CartCommandService commandService;

   @GetMapping
   public CartDetails getCart() {
      return retrievalService.getCart( userId() );
   }

   @PostMapping( "/items" )
   public CartDetails addItem( @RequestBody @Valid AddToCartRequest req ) {
      return commandService.addItem( userId(), req );
   }

   @PutMapping( "/items/{productId}" )
   public CartDetails updateQuantity(
      @PathVariable Long productId,
      @RequestBody @Valid UpdateCartItemRequest req
   ) {
      return commandService.updateItemQuantity( userId(), productId, req.getQuantity() );
   }

   @DeleteMapping( "/items/{productId}" )
   public ResponseEntity< Void > removeItem( @PathVariable Long productId ) {
      commandService.removeItem( userId(), productId );
      return ResponseEntity.noContent().build();
   }

   @DeleteMapping
   public ResponseEntity< Void > clearCart() {
      commandService.clearCart( userId() );
      return ResponseEntity.noContent().build();
   }

   private String userId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
