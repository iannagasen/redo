package dev.agasen.core.payment;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.agasen.api.payment.read.PaymentDetails;
import dev.agasen.core.payment.application.IdempotencyStore;
import dev.agasen.core.payment.repository.IdempotencyRecordRepository;
import dev.agasen.core.payment.repository.entity.IdempotencyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdempotencyStoreTest {

   private IdempotencyRecordRepository repository;
   private IdempotencyStore store;

   @BeforeEach
   void setUp() {
      repository = mock( IdempotencyRecordRepository.class );
      var jsonMapper = JsonMapper.builder().build();
      store = new IdempotencyStore( repository, jsonMapper );
   }

   @Test
   void find_returnsEmpty_whenNoRecordExists() {
      when( repository.findByIdempotencyKeyAndUserId( any(), any() ) ).thenReturn( Optional.empty() );

      var result = store.find( UUID.randomUUID(), "user-1" );

      assertThat( result ).isEmpty();
   }

   @Test
   void find_returnsDeserializedPaymentDetails_whenRecordExists() {
      UUID key = UUID.randomUUID();
      String json = """
         {
           "id": 42,
           "orderId": 10,
           "userId": "user-1",
           "amount": 99.99,
           "currency": "USD",
           "status": "CAPTURED",
           "createdAt": null
         }
         """;

      IdempotencyRecord record = new IdempotencyRecord();
      record.setIdempotencyKey( key );
      record.setUserId( "user-1" );
      record.setResponseBody( json );

      when( repository.findByIdempotencyKeyAndUserId( key, "user-1" ) ).thenReturn( Optional.of( record ) );

      var result = store.find( key, "user-1" );

      assertThat( result ).isPresent();
      assertThat( result.get().getId() ).isEqualTo( 42L );
      assertThat( result.get().getOrderId() ).isEqualTo( 10L );
      assertThat( result.get().getStatus() ).isEqualTo( "CAPTURED" );
      assertThat( result.get().getAmount() ).isEqualByComparingTo( new BigDecimal( "99.99" ) );
   }

   @Test
   void save_persistsSerializedPaymentDetails() {
      UUID key = UUID.randomUUID();
      when( repository.save( any() ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

      PaymentDetails details = new PaymentDetails();
      details.setId( 1L );
      details.setOrderId( 5L );
      details.setUserId( "user-1" );
      details.setAmount( new BigDecimal( "50.00" ) );
      details.setCurrency( "USD" );
      details.setStatus( "CAPTURED" );

      store.save( key, "user-1", details );

      ArgumentCaptor< IdempotencyRecord > captor = ArgumentCaptor.forClass( IdempotencyRecord.class );
      verify( repository ).save( captor.capture() );

      IdempotencyRecord saved = captor.getValue();
      assertThat( saved.getIdempotencyKey() ).isEqualTo( key );
      assertThat( saved.getUserId() ).isEqualTo( "user-1" );
      assertThat( saved.getResponseBody() ).contains( """
         "id":1""" );
      assertThat( saved.getResponseBody() ).contains( """
         "orderId":5""" );
      assertThat( saved.getResponseBody() ).contains( """
         "status":"CAPTURED\"""" );
   }

   @Test
   void save_thenFind_returnsOriginalPaymentDetails() {
      UUID key = UUID.randomUUID();

      PaymentDetails original = new PaymentDetails();
      original.setId( 7L );
      original.setOrderId( 3L );
      original.setUserId( "user-1" );
      original.setAmount( new BigDecimal( "25.00" ) );
      original.setCurrency( "EUR" );
      original.setStatus( "CAPTURED" );

      ArgumentCaptor< IdempotencyRecord > captor = ArgumentCaptor.forClass( IdempotencyRecord.class );
      when( repository.save( captor.capture() ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

      store.save( key, "user-1", original );

      // Wire the saved record back into find
      when( repository.findByIdempotencyKeyAndUserId( key, "user-1" ) )
         .thenReturn( Optional.of( captor.getValue() ) );

      var found = store.find( key, "user-1" );

      assertThat( found ).isPresent();
      assertThat( found.get().getId() ).isEqualTo( 7L );
      assertThat( found.get().getStatus() ).isEqualTo( "CAPTURED" );
      assertThat( found.get().getAmount() ).isEqualByComparingTo( new BigDecimal( "25.00" ) );
   }
}
