package dev.agasen.core.payment;

import dev.agasen.api.core.payment.write.InitiatePaymentRequest;
import dev.agasen.api.core.payment.read.PaymentDetails;
import dev.agasen.core.payment.application.IdempotencyStore;
import dev.agasen.core.payment.application.PaymentInitiator;
import dev.agasen.core.payment.application.PaymentRetriever;
import dev.agasen.core.payment.event.PaymentEventPublisher;
import dev.agasen.core.payment.gateway.PaymentGatewayClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the idempotency check in {@link PaymentRestService#initiatePayment}.
 * <p>
 * What's being tested:
 * - A new key hits the initiator and the result is persisted in the idempotency store.
 * - A duplicate key returns the cached response without invoking the initiator again.
 * - A missing Idempotency-Key header is rejected with 400.
 * <p>
 * Infrastructure: H2 (via application-test.yml), @EmbeddedKafka required by the test profile.
 * PaymentInitiator and IdempotencyStore are mocked so Kafka and the gateway are never hit.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
@EmbeddedKafka( partitions = 1, topics = { PaymentEventPublisher.TOPIC } )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
class PaymentRestServiceIdempotencyIT {

   @Autowired private MockMvc mockMvc;
   @Autowired private JsonMapper jsonMapper;

   @MockitoBean private PaymentInitiator paymentInitiator;
   @MockitoBean private IdempotencyStore idempotencyStore;
   @MockitoBean private PaymentRetriever paymentRetriever;
   @MockitoBean private PaymentGatewayClient paymentGatewayClient;

   private static final String USER_ID = "user-1";

   @Test
   void initiatePayment_whenKeyIsNew_callsInitiatorAndPersistsResult() throws Exception {
      UUID key = UUID.randomUUID();
      PaymentDetails result = buildPaymentDetails( 1L, "CAPTURED" );

      when( idempotencyStore.find( key, USER_ID ) ).thenReturn( Optional.empty() );
      when( paymentInitiator.initiatePayment( eq( USER_ID ), any() ) ).thenReturn( result );

      mockMvc.perform( post( "/api/v1/payments" )
            .contentType( MediaType.APPLICATION_JSON )
            .content( jsonMapper.writeValueAsString( buildRequest() ) )
            .header( "Idempotency-Key", key )
            .with( jwt().jwt( b -> b.subject( USER_ID ) ) ) )
         .andExpect( status().isCreated() )
         .andExpect( jsonPath( "$.id" ).value( 1L ) )
         .andExpect( jsonPath( "$.status" ).value( "CAPTURED" ) );

      verify( paymentInitiator ).initiatePayment( eq( USER_ID ), any() );
      verify( idempotencyStore ).save( eq( key ), eq( USER_ID ), eq( result ) );
   }

   @Test
   void initiatePayment_whenKeyAlreadyExists_returnsCachedResultWithoutCallingInitiator() throws Exception {
      UUID key = UUID.randomUUID();
      PaymentDetails cached = buildPaymentDetails( 42L, "CAPTURED" );

      when( idempotencyStore.find( key, USER_ID ) ).thenReturn( Optional.of( cached ) );

      mockMvc.perform( post( "/api/v1/payments" )
            .contentType( MediaType.APPLICATION_JSON )
            .content( jsonMapper.writeValueAsString( buildRequest() ) )
            .header( "Idempotency-Key", key )
            .with( jwt().jwt( b -> b.subject( USER_ID ) ) ) )
         .andExpect( status().isCreated() )
         .andExpect( jsonPath( "$.id" ).value( 42L ) );

      verify( paymentInitiator, never() ).initiatePayment( any(), any() );
      verify( idempotencyStore, never() ).save( any(), any(), any() );
   }

   @Test
   void initiatePayment_whenIdempotencyKeyHeaderMissing_returnsBadRequest() throws Exception {
      mockMvc.perform( post( "/api/v1/payments" )
            .contentType( MediaType.APPLICATION_JSON )
            .content( jsonMapper.writeValueAsString( buildRequest() ) )
            .with( jwt().jwt( b -> b.subject( USER_ID ) ) ) )
         .andExpect( status().isBadRequest() );
   }


   /**
    * The same key sent by two different users must be treated as independent requests.
    * The store lookup is keyed on (idempotencyKey, userId), so user-2 should not
    * receive user-1's cached result.
    */
   @Test
   void initiatePayment_sameKey_differentUsers_areIndependentRequests() throws Exception {
      UUID sharedKey = UUID.randomUUID();
      PaymentDetails user1Result = buildPaymentDetails( 10L, "CAPTURED" );
      PaymentDetails user2Result = buildPaymentDetails( 20L, "CAPTURED" );

      when( idempotencyStore.find( sharedKey, "user-1" ) ).thenReturn( Optional.empty() );
      when( idempotencyStore.find( sharedKey, "user-2" ) ).thenReturn( Optional.empty() );
      when( paymentInitiator.initiatePayment( eq( "user-1" ), any() ) ).thenReturn( user1Result );
      when( paymentInitiator.initiatePayment( eq( "user-2" ), any() ) ).thenReturn( user2Result );

      // user-1 request
      mockMvc.perform( post( "/api/v1/payments" )
            .contentType( MediaType.APPLICATION_JSON )
            .content( jsonMapper.writeValueAsString( buildRequest() ) )
            .header( "Idempotency-Key", sharedKey )
            .with( jwt().jwt( b -> b.subject( "user-1" ) ) ) )
         .andExpect( status().isCreated() )
         .andExpect( jsonPath( "$.id" ).value( 10L ) );

      // user-2 request with the same key
      mockMvc.perform( post( "/api/v1/payments" )
            .contentType( MediaType.APPLICATION_JSON )
            .content( jsonMapper.writeValueAsString( buildRequest() ) )
            .header( "Idempotency-Key", sharedKey )
            .with( jwt().jwt( b -> b.subject( "user-2" ) ) ) )
         .andExpect( status().isCreated() )
         .andExpect( jsonPath( "$.id" ).value( 20L ) );

      verify( paymentInitiator ).initiatePayment( eq( "user-1" ), any() );
      verify( paymentInitiator ).initiatePayment( eq( "user-2" ), any() );
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private PaymentDetails buildPaymentDetails( Long id, String status ) {
      PaymentDetails d = new PaymentDetails();
      d.setId( id );
      d.setOrderId( 10L );
      d.setUserId( USER_ID );
      d.setAmount( new BigDecimal( "99.99" ) );
      d.setCurrency( "USD" );
      d.setStatus( status );
      return d;
   }

   private InitiatePaymentRequest buildRequest() {
      InitiatePaymentRequest r = new InitiatePaymentRequest();
      r.setOrderId( 10L );
      r.setAmount( new BigDecimal( "99.99" ) );
      r.setCurrency( "USD" );
      r.setCardNumber( "4111111111111111" );
      r.setCardholderName( "Test User" );
      r.setExpiryMonth( 12 );
      r.setExpiryYear( 2030 );
      r.setCvv( "123" );
      return r;
   }
}
