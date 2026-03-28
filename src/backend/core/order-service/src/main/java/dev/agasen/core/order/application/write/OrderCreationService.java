package dev.agasen.core.order.application.write;

import dev.agasen.api.core.order.write.CreateOrderRequest;
import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.api.core.order.write.OrderItemRequest;
import dev.agasen.common.http.exceptions.BadRequestException;
import dev.agasen.common.http.exceptions.Exceptions;
import dev.agasen.core.order.domain.Order;
import dev.agasen.core.order.domain.OrderItem;
import dev.agasen.core.order.domain.OrderRepository;
import dev.agasen.core.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static dev.agasen.core.order.application.mapper.OrderDetailsMapper.toOrderDetails;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCreationService {

   private final OrderRepository orderRepository;

   public OrderDetails createOrder( String userId, CreateOrderRequest request ) {
      Order order = new Order();
      order.setUserId( userId );
      order.setStatus( OrderStatus.PENDING );

      BigDecimal total = BigDecimal.ZERO;

      for ( OrderItemRequest itemReq : request.getItems() ) {
         OrderItem item = new OrderItem();
         item.setOrder( order );
         item.setProductId( itemReq.getProductId() );
         item.setProductName( itemReq.getProductName() );
         item.setBrand( itemReq.getBrand() );
         item.setPrice( itemReq.getPrice() );
         item.setCurrency( itemReq.getCurrency() );
         item.setQuantity( itemReq.getQuantity() );

         BigDecimal lineTotal = itemReq.getPrice().multiply( BigDecimal.valueOf( itemReq.getQuantity() ) );
         item.setLineTotal( lineTotal );
         total = total.add( lineTotal );

         order.getItems().add( item );
      }

      order.setTotal( total );
      Order saved = orderRepository.save( order );

      return toOrderDetails( saved );
   }

   public OrderDetails updateStatus( Long orderId, String status ) {
      Order order = orderRepository.findById( orderId )
         .orElseThrow( Exceptions.notFound( "Order", orderId ) );

      try {
         order.setStatus( OrderStatus.valueOf( status.toUpperCase() ) );
      } catch ( IllegalArgumentException e ) {
         throw new BadRequestException( "Invalid order status: " + status );
      }

      Order saved = orderRepository.save( order );
      return toOrderDetails( saved );
   }
}
