package dev.agasen.core.order.application.mapper;

import dev.agasen.api.core.order.read.OrderItemDetails;
import dev.agasen.api.core.order.read.OrderSummaryItem;
import dev.agasen.api.core.payment.read.PaymentDetails;
import dev.agasen.api.core.product.product.ProductDetails;
import dev.agasen.common.Result;

public class OrderSummaryMapper {

   static OrderSummaryItem toSummaryItem( OrderItemDetails item, Result< ProductDetails > result ) {
      var description = switch ( result ) {
         case Result.Success< ProductDetails > s -> s.value().getDescription();
         case Result.Failure< ProductDetails > f -> null;
      };
      return new OrderSummaryItem(
         item.getProductId(),
         item.getProductName(),
         item.getBrand(),
         description,
         item.getPrice(),
         item.getCurrency(),
         item.getQuantity(),
         item.getLineTotal()
      );
   }

   static PaymentDetails toPaymentDetails( Result< PaymentDetails > result ) {
      return switch ( result ) {
         case Result.Success< PaymentDetails > s -> s.value();
         case Result.Failure< PaymentDetails > f -> null;
      };
   }
}
