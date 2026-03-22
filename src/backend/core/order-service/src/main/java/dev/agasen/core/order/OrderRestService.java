package dev.agasen.core.order;

import dev.agasen.api.order.CreateOrderRequest;
import dev.agasen.api.order.OrderApi;
import dev.agasen.api.order.OrderDetails;
import dev.agasen.api.order.OrderSummary;
import dev.agasen.api.order.UpdateOrderStatusRequest;
import dev.agasen.core.order.application.read.OrderQueryService;
import dev.agasen.core.order.application.read.OrderSummaryQueryService;
import dev.agasen.core.order.application.write.OrderCommandService;
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
   private final OrderCommandService orderCommandService;
   private final OrderSummaryQueryService orderSummaryQueryService;

   public List< OrderDetails > getOrders() {
      return orderQueryService.getOrders( currentUserId() );
   }

   public OrderDetails getOrderById( Long id ) {
      return orderQueryService.getOrderById( id );
   }

   @ResponseStatus( HttpStatus.CREATED )
   public OrderDetails createOrder( CreateOrderRequest request ) {
      return orderCommandService.createOrder( currentUserId(), request );
   }

   public OrderDetails updateStatus( Long id, UpdateOrderStatusRequest request ) {
      return orderCommandService.updateStatus( id, request.getStatus() );
   }

   public OrderSummary getOrderSummary( Long id ) {
      return orderSummaryQueryService.getOrderSummary( id );
   }

   private String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
