package dev.agasen.core.order.application.read;

import dev.agasen.api.order.OrderDetails;
import dev.agasen.api.order.OrderItemDetails;
import dev.agasen.common.exceptions.Exceptions;
import dev.agasen.core.order.domain.Order;
import dev.agasen.core.order.domain.OrderItem;
import dev.agasen.core.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true )
public class OrderRetrievalService {

   private final OrderRepository orderRepository;

   public List<OrderDetails> getOrders( String userId ) {
      return orderRepository.findByUserIdOrderByCreatedAtDesc( userId )
         .stream()
         .map( OrderRetrievalService::toOrderDetails )
         .toList();
   }

   public OrderDetails getOrderById( String userId, Long id ) {
      Order order = orderRepository.findById( id )
         .orElseThrow( Exceptions.notFound( "Order", id ) );

      if ( !order.getUserId().equals( userId ) ) {
         throw Exceptions.notFound( "Order", id ).get();
      }

      return toOrderDetails( order );
   }

   public static OrderDetails toOrderDetails( Order order ) {
      OrderDetails details = new OrderDetails();
      details.setId( order.getId() );
      details.setUserId( order.getUserId() );
      details.setStatus( order.getStatus().name() );
      details.setTotal( order.getTotal() );
      details.setCreatedAt( order.getCreatedAt() );

      List<OrderItemDetails> itemDetails = order.getItems().stream()
         .map( OrderRetrievalService::toOrderItemDetails )
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
