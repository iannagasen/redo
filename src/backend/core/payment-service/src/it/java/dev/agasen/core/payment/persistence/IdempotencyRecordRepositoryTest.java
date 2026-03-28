package dev.agasen.core.payment.persistence;

import dev.agasen.core.payment.repository.IdempotencyRecordRepository;
import dev.agasen.core.payment.repository.entity.IdempotencyRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class IdempotencyRecordRepositoryTest {

   @Autowired IdempotencyRecordRepository repository;

   @Test
   void findByKeyAndUserId_returnsRecord_whenExists() {
      UUID key = UUID.randomUUID();
      repository.saveAndFlush( record( key, "user-1", "{\"id\":1}" ) );

      var found = repository.findByIdempotencyKeyAndUserId( key, "user-1" );

      assertThat( found ).isPresent();
      assertThat( found.get().getResponseBody() ).isEqualTo( "{\"id\":1}" );
   }

   @Test
   void findByKeyAndUserId_returnsEmpty_whenNotFound() {
      var found = repository.findByIdempotencyKeyAndUserId( UUID.randomUUID(), "user-1" );

      assertThat( found ).isEmpty();
   }

   @Test
   void uniqueConstraint_rejectsInsert_whenSameKeyAndUser() {
      UUID key = UUID.randomUUID();
      repository.saveAndFlush( record( key, "user-1", "{\"id\":1}" ) );

      assertThrows( Exception.class, () ->
         repository.saveAndFlush( record( key, "user-1", "{\"id\":2}" ) )
      );
   }

   @Test
   void sameKey_differentUser_isAllowed() {
      UUID key = UUID.randomUUID();

      assertDoesNotThrow( () -> {
         repository.saveAndFlush( record( key, "user-1", "{\"id\":1}" ) );
         repository.saveAndFlush( record( key, "user-2", "{\"id\":2}" ) );
      } );
   }

   private IdempotencyRecord record( UUID key, String userId, String responseBody ) {
      IdempotencyRecord r = new IdempotencyRecord();
      r.setIdempotencyKey( key );
      r.setUserId( userId );
      r.setResponseBody( responseBody );
      return r;
   }
}
