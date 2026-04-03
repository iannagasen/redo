package dev.agasen.core.order.domain.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SagaStateRepository extends JpaRepository< SagaState, UUID > {

   /**
    * Loads the saga state together with its participants in a single JOIN FETCH query.
    * Use this whenever you need to read or mutate participant statuses.
    */
   @Query( "SELECT s FROM SagaState s JOIN FETCH s.participants WHERE s.orderId = :orderId" )
   Optional< SagaState > findByOrderIdWithParticipants( @Param( "orderId" ) Long orderId );


}
