package dev.agasen.core.payment.event;

import dev.agasen.api.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

   public static final String TOPIC = "payment.result";

   private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

   public void publish( PaymentEvent event ) {
      log.info( "Publishing PaymentEvent to topic '{}': orderId={}, status={}", TOPIC, event.orderId(), event.status() );
      kafkaTemplate.send( TOPIC, String.valueOf( event.orderId() ), event );
   }
}
