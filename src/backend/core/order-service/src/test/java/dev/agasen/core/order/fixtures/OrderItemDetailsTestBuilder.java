package dev.agasen.core.order.fixtures;

import dev.agasen.api.core.order.read.OrderItemDetails;

import java.math.BigDecimal;

public class OrderItemDetailsTestBuilder {

   public static OrderItemDetails usdOrderItem( Long productId, String name, String brand, BigDecimal price, int qty ) {
      var item = new OrderItemDetails();
      item.setProductId( productId );
      item.setProductName( name );
      item.setBrand( brand );
      item.setPrice( price );
      item.setCurrency( "USD" );
      item.setQuantity( qty );
      item.setLineTotal( price.multiply( BigDecimal.valueOf( qty ) ) );
      return item;
   }
}
