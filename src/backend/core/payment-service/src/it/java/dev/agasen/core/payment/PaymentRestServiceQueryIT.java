package dev.agasen.core.payment;

import dev.agasen.core.payment.application.IdempotencyStore;
import dev.agasen.core.payment.application.PaymentInitiator;
import dev.agasen.core.payment.application.PaymentRetriever;
import dev.agasen.core.payment.event.PaymentEventPublisher;
import dev.agasen.core.payment.gateway.PaymentGatewayClient;
import dev.agasen.platform.core.http.exceptions.NotFoundException;
import dev.agasen.platform.contracts.GlobalExceptionHandling;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying that query endpoints return 404 (not 500) when a payment
 * does not exist. Relies on {@link GlobalExceptionHandling} being
 * registered via platform:api auto-configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
@EmbeddedKafka( partitions = 1, topics = { PaymentEventPublisher.TOPIC } )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
class PaymentRestServiceQueryIT {

   @Autowired private MockMvc mockMvc;

   @MockitoBean private PaymentRetriever paymentRetriever;
   @MockitoBean private PaymentInitiator paymentInitiator;
   @MockitoBean private IdempotencyStore idempotencyStore;
   @MockitoBean private PaymentGatewayClient paymentGatewayClient;

   @Test
   void getPaymentById_whenNotFound_returns404() throws Exception {
      when( paymentRetriever.getPaymentById( 999L ) )
         .thenThrow( new NotFoundException( "Payment with id: 999" ) );

      mockMvc.perform( get( "/api/v1/payments/999" )
            .with( jwt().jwt( b -> b.subject( "user-1" ) ) ) )
         .andExpect( status().isNotFound() )
         .andExpect( jsonPath( "$.status" ).value( 404 ) )
         .andExpect( jsonPath( "$.message" ).value( "Payment with id: 999" ) );
   }

   @Test
   void getPaymentByOrderId_whenNotFound_returns404() throws Exception {
      when( paymentRetriever.getPaymentByOrderId( 201L ) )
         .thenThrow( new NotFoundException( "Payment with id: 201" ) );

      mockMvc.perform( get( "/api/v1/payments/order/201" )
            .with( jwt().jwt( b -> b.subject( "user-1" ) ) ) )
         .andExpect( status().isNotFound() )
         .andExpect( jsonPath( "$.status" ).value( 404 ) )
         .andExpect( jsonPath( "$.message" ).value( "Payment with id: 201" ) );
   }
}
