package dev.agasen.core.cart.application.read;

import dev.agasen.api.cart.CartDetails;
import dev.agasen.api.cart.CartItemDetails;
import dev.agasen.core.cart.domain.Cart;
import dev.agasen.core.cart.domain.CartItem;
import dev.agasen.core.cart.domain.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional( readOnly = true )
@RequiredArgsConstructor
public class CartRetrievalService {

   private final CartRepository cartRepository;

   @PreAuthorize( "hasAnyAuthority('SCOPE_openid')" )
   public CartDetails getCart( String userId ) {
      Cart cart = cartRepository.findByUserId( userId )
         .orElseGet( () -> emptyCart( userId ) );
      return toCartDetails( cart );
   }

   private Cart emptyCart( String userId ) {
      Cart cart = new Cart();
      cart.setUserId( userId );
      cart.setItems( new ArrayList<>() );
      cart.setUpdatedAt( Instant.now() );
      return cart;
   }

   public static CartDetails toCartDetails( Cart cart ) {
      List< CartItemDetails > itemDetails = cart.getItems().stream()
         .map( CartRetrievalService::toCartItemDetails )
         .toList();

      BigDecimal total = itemDetails.stream()
         .map( CartItemDetails::getLineTotal )
         .reduce( BigDecimal.ZERO, BigDecimal::add );

      int itemCount = cart.getItems().stream()
         .mapToInt( CartItem::getQuantity )
         .sum();

      CartDetails details = new CartDetails();
      details.setUserId( cart.getUserId() );
      details.setItems( itemDetails );
      details.setTotal( total );
      details.setItemCount( itemCount );
      return details;
   }

   private static CartItemDetails toCartItemDetails( CartItem item ) {
      CartItemDetails details = new CartItemDetails();
      details.setProductId( item.getProductId() );
      details.setProductName( item.getProductName() );
      details.setBrand( item.getBrand() );
      details.setPrice( item.getPrice() );
      details.setCurrency( item.getCurrency() );
      details.setQuantity( item.getQuantity() );
      details.setLineTotal( item.getPrice().multiply( BigDecimal.valueOf( item.getQuantity() ) ) );
      return details;
   }
}
