package dev.agasen.core.order.fixtures;

import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.api.core.order.read.OrderItemDetails;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDetailsTestBuilder {

   public static OrderDetails pendingOrderDetails( Long id, List< OrderItemDetails > items ) {
      var order = new OrderDetails();
      order.setId( id );
      order.setUserId( "user-1" );
      order.setStatus( "PENDING" );
      order.setTotal( items.stream().map( OrderItemDetails::getLineTotal ).reduce( BigDecimal.ZERO, BigDecimal::add ) );
      order.setItemCount( items.stream().mapToInt( OrderItemDetails::getQuantity ).sum() );
      order.setItems( items );
      order.setCreatedAt( Instant.now() );
      return order;
   }
}
