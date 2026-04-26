package dev.agasen.core.order.application.mapper;

import dev.agasen.platform.contracts.core.order.read.OrderItemDetails;
import dev.agasen.platform.contracts.core.order.read.OrderSummaryItem;
import dev.agasen.platform.contracts.core.payment.read.PaymentDetails;
import dev.agasen.platform.contracts.core.product.product.ProductDetails;
import dev.agasen.platform.core.Result;

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
