package dev.agasen.core.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.agasen.api.order.CreateOrderRequest;
import dev.agasen.api.order.OrderDetails;
import dev.agasen.api.order.UpdateOrderStatusRequest;
import dev.agasen.core.order.application.read.OrderRetrievalService;
import dev.agasen.core.order.application.write.OrderCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping( "/api/v1/orders" )
@RequiredArgsConstructor
public class OrderRestService {

   private final OrderRetrievalService orderRetrievalService;
   private final OrderCommandService orderCommandService;

   @GetMapping
   public List< OrderDetails > getOrders() {
      return orderRetrievalService.getOrders( currentUserId() );
   }

   @GetMapping( "/{id}" )
   public OrderDetails getOrderById( @PathVariable Long id ) {
      return orderRetrievalService.getOrderById( currentUserId(), id );
   }

   @PostMapping
   @ResponseStatus( HttpStatus.CREATED )
   public OrderDetails createOrder( @RequestBody @Valid CreateOrderRequest request ) {
      return orderCommandService.createOrder( currentUserId(), request );
   }

   @PutMapping( "/{id}/status" )
   public OrderDetails updateStatus(
      @PathVariable Long id,
      @RequestBody @Valid UpdateOrderStatusRequest request
   ) {
      return orderCommandService.updateStatus( id, request.getStatus() );
   }

   private String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
