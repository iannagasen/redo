package dev.agasen.core.payment.application;

import dev.agasen.api.core.payment.read.PaymentDetails;
import dev.agasen.core.payment.repository.IdempotencyRecordRepository;
import dev.agasen.core.payment.repository.entity.IdempotencyRecord;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyStore {

   private final IdempotencyRecordRepository repository;
   private final JsonMapper jsonMapper;

   public IdempotencyStore( IdempotencyRecordRepository repository, JsonMapper jsonMapper ) {
      this.repository = repository;
      this.jsonMapper = jsonMapper;
   }

   public Optional< PaymentDetails > find( UUID key, String userId ) {
      return repository.findByIdempotencyKeyAndUserId( key, userId )
         .map( record -> deserialize( record.getResponseBody() ) );
   }

   public void save( UUID key, String userId, PaymentDetails response ) {
      IdempotencyRecord record = new IdempotencyRecord();
      record.setIdempotencyKey( key );
      record.setUserId( userId );
      record.setResponseBody( jsonMapper.writeValueAsString( response ) );
      repository.save( record );
   }

   private PaymentDetails deserialize( String json ) {
      return jsonMapper.readValue( json, PaymentDetails.class );
   }

}
