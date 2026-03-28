package dev.agasen.core.order.application.mapper;

import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.api.core.order.read.OrderItemDetails;
import dev.agasen.core.order.domain.Order;
import dev.agasen.core.order.domain.OrderItem;

import java.util.List;

public class OrderDetailsMapper {
   public static OrderDetails toOrderDetails( Order order ) {
      OrderDetails details = new OrderDetails();
      details.setId( order.getId() );
      details.setUserId( order.getUserId() );
      details.setStatus( order.getStatus().name() );
      details.setTotal( order.getTotal() );
      details.setCreatedAt( order.getCreatedAt() );

      List< OrderItemDetails > itemDetails = order.getItems().stream()
         .map( OrderDetailsMapper::toOrderItemDetails )
         .toList();
      details.setItems( itemDetails );
      details.setItemCount( itemDetails.stream().mapToInt( OrderItemDetails::getQuantity ).sum() );

      return details;
   }

   private static OrderItemDetails toOrderItemDetails( OrderItem item ) {
      OrderItemDetails details = new OrderItemDetails();
      details.setProductId( item.getProductId() );
      details.setProductName( item.getProductName() );
      details.setBrand( item.getBrand() );
      details.setPrice( item.getPrice() );
      details.setCurrency( item.getCurrency() );
      details.setQuantity( item.getQuantity() );
      details.setLineTotal( item.getLineTotal() );
      return details;
   }
}
