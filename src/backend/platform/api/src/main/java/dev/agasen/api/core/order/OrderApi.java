package dev.agasen.api.core.order;

import dev.agasen.api.core.order.read.OrderDetails;
import dev.agasen.api.core.order.read.OrderSummary;
import dev.agasen.api.core.order.write.CreateOrderRequest;
import dev.agasen.api.core.order.write.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@HttpExchange( "/api/v1/orders" )
public interface OrderApi {

   @GetExchange
   List< OrderDetails > getOrders();

   @GetExchange( "/{id}" )
   OrderDetails getOrderById( @PathVariable Long id );

   @PostExchange
   OrderDetails createOrder( @RequestBody @Valid CreateOrderRequest request );

   @PutExchange( "/{id}/status" )
   OrderDetails updateStatus( @PathVariable Long id, @RequestBody @Valid UpdateOrderStatusRequest request );

   @GetExchange( "/{id}/summary" )
   OrderSummary getOrderSummary( @PathVariable Long id );
}
