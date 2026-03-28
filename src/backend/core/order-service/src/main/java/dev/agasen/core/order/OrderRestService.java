package dev.agasen.core.order;

import dev.agasen.api.core.order.write.CreateOrderRequest;
import dev.agasen.api.core.order.OrderApi;
import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.api.core.order.read.OrderSummary;
import dev.agasen.api.core.order.write.UpdateOrderStatusRequest;
import dev.agasen.core.order.application.read.OrderQueryService;
import dev.agasen.core.order.application.read.OrderSummaryAggregator;
import dev.agasen.core.order.application.write.OrderCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderRestService implements OrderApi {

   private final OrderQueryService orderQueryService;
   private final OrderCreationService orderCreationService;
   private final OrderSummaryAggregator orderSummaryAggregator;

   public List< OrderDetails > getOrders() {
      return orderQueryService.getOrders( currentUserId() );
   }

   public OrderDetails getOrderById( Long id ) {
      return orderQueryService.getOrderById( id );
   }

   @ResponseStatus( HttpStatus.CREATED )
   public OrderDetails createOrder( CreateOrderRequest request ) {
      return orderCreationService.createOrder( currentUserId(), request );
   }

   public OrderDetails updateStatus( Long id, UpdateOrderStatusRequest request ) {
      return orderCreationService.updateStatus( id, request.getStatus() );
   }

   public OrderSummary getOrderSummary( Long id ) {
      return orderSummaryAggregator.getOrderSummary( id );
   }

   private String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
