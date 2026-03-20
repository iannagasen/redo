package dev.agasen.core.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

   /**
    * Spring Boot 3.x auto-wires any {@link CommonErrorHandler} bean into the
    * auto-configured {@link org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory}.
    *
    * Retry policy: up to 2 retries with 1-second backoff.
    * After the 3rd failure the message is forwarded to the Dead Letter Topic:
    *   "payment.result" → "payment.result.DLT"
    *
    * SerializationExceptions are not retried — malformed bytes will never fix
    * themselves and retrying them only delays the inevitable DLQ write.
    */
   @Bean
   public CommonErrorHandler kafkaErrorHandler( KafkaTemplate<Object, Object> kafkaTemplate ) {
      var recoverer = new DeadLetterPublishingRecoverer( kafkaTemplate );
      var errorHandler = new DefaultErrorHandler( recoverer, new FixedBackOff( 1000L, 2 ) );
      errorHandler.addNotRetryableExceptions( org.apache.kafka.common.errors.SerializationException.class );
      return errorHandler;
   }
}
