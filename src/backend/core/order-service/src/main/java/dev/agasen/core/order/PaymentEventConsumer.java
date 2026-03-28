package dev.agasen.core.order;

import dev.agasen.api.core.event.PaymentEvent;
import dev.agasen.core.order.application.write.OrderCommandService;
import dev.agasen.core.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

   private final OrderCommandService orderCommandService;
   private final OrderRepository orderRepository;

   @KafkaListener( topics = "payment.result", groupId = "order-service" )
   public void onPaymentResult( PaymentEvent event ) {
      log.info( "Received PaymentEvent: orderId={}, status={}", event.orderId(), event.status() );
      Objects.requireNonNull( event.status(), "PaymentEvent.status() must not be null" );

      if ( checkIdempotency( event.orderId() ) ) {
         log.warn( "Skipping duplicate PaymentEvent for orderId={}", event.orderId() );
         return;
      }

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

   private boolean checkIdempotency( Long id ) {
      return orderRepository.findById( id )
         .map( o -> Set.of( "CONFIRMED", "PAYMENT_FAILED" ).contains( o.getStatus().name() ) )
         .orElse( false );
   }

}
