package dev.agasen.core.cart.application.write;

import dev.agasen.api.cart.AddToCartRequest;
import dev.agasen.api.cart.CartDetails;
import dev.agasen.core.cart.application.read.CartRetrievalService;
import dev.agasen.core.cart.domain.Cart;
import dev.agasen.core.cart.domain.CartItem;
import dev.agasen.core.cart.domain.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartCommandService {

   private final CartRepository cartRepository;

   @PreAuthorize( "hasAnyAuthority('SCOPE_openid')" )
   public CartDetails addItem( String userId, AddToCartRequest req ) {
      Cart cart = cartRepository.findByUserId( userId )
         .orElseGet( () -> {
            Cart c = new Cart();
            c.setUserId( userId );
            c.setItems( new ArrayList<>() );
            return c;
         } );

      CartItem item = fromRequest( req );
      cart.addItem( item );
      cart.setUpdatedAt( Instant.now() );
      cartRepository.save( userId, cart );
      return CartRetrievalService.toCartDetails( cart );
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_openid')" )
   public CartDetails updateItemQuantity( String userId, Long productId, int quantity ) {
      Cart cart = cartRepository.findByUserId( userId )
         .orElseGet( () -> {
            Cart c = new Cart();
            c.setUserId( userId );
            c.setItems( new ArrayList<>() );
            return c;
         } );

      if ( quantity <= 0 ) {
         cart.removeItem( productId );
      } else {
         cart.updateQuantity( productId, quantity );
      }
      cart.setUpdatedAt( Instant.now() );
      cartRepository.save( userId, cart );
      return CartRetrievalService.toCartDetails( cart );
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_openid')" )
   public void removeItem( String userId, Long productId ) {
      cartRepository.findByUserId( userId ).ifPresent( cart -> {
         cart.removeItem( productId );
         cart.setUpdatedAt( Instant.now() );
         cartRepository.save( userId, cart );
      } );
   }

   @PreAuthorize( "hasAnyAuthority('SCOPE_openid')" )
   public void clearCart( String userId ) {
      cartRepository.delete( userId );
   }

   private CartItem fromRequest( AddToCartRequest req ) {
      CartItem item = new CartItem();
      item.setProductId( req.getProductId() );
      item.setProductName( req.getProductName() );
      item.setBrand( req.getBrand() );
      item.setPrice( req.getPrice() );
      item.setCurrency( req.getCurrency() );
      item.setQuantity( req.getQuantity() );
      return item;
   }
}
