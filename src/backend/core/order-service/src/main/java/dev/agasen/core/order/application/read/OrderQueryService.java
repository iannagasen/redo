package dev.agasen.core.order.application.read;

import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.common.http.exceptions.Exceptions;
import dev.agasen.core.order.application.mapper.OrderDetailsMapper;
import dev.agasen.core.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true )
public class OrderQueryService {

   private final OrderRepository orderRepository;

   public List< OrderDetails > getOrders( String userId ) {
      return orderRepository.findByUserIdOrderByCreatedAtDesc( userId )
         .stream()
         .map( OrderDetailsMapper::toOrderDetails )
         .toList();
   }

   public OrderDetails getOrderById( Long id ) {
      return orderRepository.findById( id )
         .map( OrderDetailsMapper::toOrderDetails )
         .orElseThrow( Exceptions.notFound( "Order", id ) );
   }

}
