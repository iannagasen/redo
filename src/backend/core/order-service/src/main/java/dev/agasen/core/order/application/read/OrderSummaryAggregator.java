package dev.agasen.core.order.application.read;

import dev.agasen.api.order.read.OrderSummary;
import dev.agasen.api.order.read.OrderSummaryItem;
import dev.agasen.api.order.read.OrderItemDetails;
import dev.agasen.api.payment.PaymentApi;
import dev.agasen.api.product.ProductApi;
import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.common.http.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderSummaryAggregator {

   private final OrderQueryService orderQueryService;
   private final ProductApi productClient;
   private final PaymentApi paymentClient;

   public OrderSummary getOrderSummary( Long id ) {
      var order = orderQueryService.getOrderById( id );

      try ( var scope = StructuredTaskScope.open( Joiner.allSuccessfulOrThrow() ) ) {
         var getPaymentTask = scope.fork( () -> paymentClient.getPaymentByOrderId( id ) );

         var productIds = order.getItems().stream().map( OrderItemDetails::getProductId ).toList();
         var getProductTask = scope.fork( () -> productClient.getProducts( productIds ) );

         scope.join();

         Map< Long, ProductDetails > productsById = getProductTask.get().stream()
            .collect( Collectors.toMap( ProductDetails::getId, p -> p ) );

         List< OrderSummaryItem > summaryItems = order.getItems().stream()
            .map( item -> toSummaryItem( item, productsById.get( item.getProductId() ) ) )
            .toList();

         return new OrderSummary( order, getPaymentTask.get(), summaryItems );
      } catch ( InterruptedException e ) {
         throw new RuntimeException( e );
      } catch ( HttpClientErrorException.NotFound e ) {
         throw new NotFoundException( "Order summary not found for order: " + id );
      }
   }

   private static OrderSummaryItem toSummaryItem( OrderItemDetails item, ProductDetails product ) {
      return new OrderSummaryItem(
         item.getProductId(),
         item.getProductName(),
         item.getBrand(),
         product != null ? product.getDescription() : null,
         item.getPrice(),
         item.getCurrency(),
         item.getQuantity(),
         item.getLineTotal()
      );
   }
}
