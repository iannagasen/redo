package dev.agasen.core.order.event;

import dev.agasen.api.event.PaymentEvent;
import dev.agasen.core.order.application.write.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

   private final OrderCommandService orderCommandService;

   @KafkaListener( topics = "payment.result", groupId = "order-service" )
   public void onPaymentResult( PaymentEvent event ) {
      log.info( "Received PaymentEvent: orderId={}, status={}", event.orderId(), event.status() );

      String newStatus = switch ( event.status() ) {
         case "CAPTURED" -> "CONFIRMED";
         case "FAILED" -> "PAYMENT_FAILED";
         default -> {
            log.warn( "Unknown payment status: {}", event.status() );
            yield null;
         }
      };

      if ( newStatus != null ) {
         orderCommandService.updateStatus( event.orderId(), newStatus );
         log.info( "Order {} updated to {}", event.orderId(), newStatus );
      }
   }
}
