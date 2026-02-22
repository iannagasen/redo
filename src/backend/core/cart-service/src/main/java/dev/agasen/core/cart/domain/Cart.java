package dev.agasen.core.cart.domain;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
   private String userId;
   private List<CartItem> items = new ArrayList<>();
   private Instant updatedAt;

   public void addItem( CartItem newItem ) {
      for ( CartItem existing : items ) {
         if ( existing.getProductId().equals( newItem.getProductId() ) ) {
            existing.setQuantity( existing.getQuantity() + newItem.getQuantity() );
            return;
         }
      }
      items.add( newItem );
   }

   public void removeItem( Long productId ) {
      items.removeIf( item -> item.getProductId().equals( productId ) );
   }

   public void updateQuantity( Long productId, int quantity ) {
      for ( CartItem item : items ) {
         if ( item.getProductId().equals( productId ) ) {
            item.setQuantity( quantity );
            return;
         }
      }
   }
}
