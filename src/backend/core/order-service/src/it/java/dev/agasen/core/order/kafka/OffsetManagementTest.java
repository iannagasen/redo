package dev.agasen.core.order.kafka;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import dev.agasen.api.core.event.PaymentEvent;

/**
 * Tests demonstrating Kafka offset management semantics:
 * D1. At-Least-Once — a crash before commitSync() causes message re-delivery
 * D2. Committed offsets — after a successful commit, messages are NOT re-delivered
 * <p>
 * These tests use raw KafkaConsumer and KafkaProducer — no Spring context needed.
 * This isolates the pure Kafka offset mechanics from Spring Kafka abstractions.
 * <p>
 * IMPORTANT: This test does NOT use @SpringBootTest intentionally.
 * The EmbeddedKafkaBroker is injected directly by @EmbeddedKafka + @SpringJUnitConfig.
 */
@SpringJUnitConfig
@EmbeddedKafka(
   partitions = 1,
   topics = { "offset-test-topic" }
)
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
class OffsetManagementTest {

   private static final String TOPIC = "offset-test-topic";
   private static final String GROUP = "offset-test-group";

   // @SpringJUnitConfig resolves beans via Spring DI, not JUnit parameter resolvers.
   // Inject EmbeddedKafkaBroker as a field — NOT as a method parameter.
   @Autowired
   private EmbeddedKafkaBroker broker;

   // ── D1. At-Least-Once Semantics ───────────────────────────────────────────

   /**
    * KAFKA MECHANISM: Offset commits and At-Least-Once delivery
    * * Kafka tracks per-group progress by storing the "committed offset" in the
    * internal __consumer_offsets topic. On restart, the consumer resumes from
    * the last committed offset.
    * <p>
    * Scenario:
    * 1. Consumer reads a message and starts processing (DB write begins)
    * 2. Service crashes mid-processing — the transaction rolls back
    * 3. Kafka offset was NEVER committed (enable-auto-commit=false)
    * 4. On restart, a new consumer with the same group.id resumes from the
    * last committed offset — it re-reads the same message
    * <p>
    * This is AT-LEAST-ONCE delivery: every message is processed at least once,
    * but possibly more times. The fix is either:
    * a) Make processing idempotent (preferred — see PaymentEventConsumerTest)
    * b) Use Exactly-Once Semantics via Kafka transactions (see D3 below)
    */
   @Test
   void atLeastOnce_messageIsRedelivered_afterCrashWithoutCommit() {
      publishEvent( broker, 42L );

      // ── Attempt 1: Read without committing (simulates crash mid-processing) ──
      try ( KafkaConsumer< String, PaymentEvent > consumer = buildConsumer( broker, GROUP ) ) {
         consumer.subscribe( List.of( TOPIC ) );
         ConsumerRecords< String, PaymentEvent > records = consumer.poll( Duration.ofSeconds( 5 ) );

         assertThat( records.count() ).isEqualTo( 1 );
         assertThat( records.iterator().next().value().orderId() ).isEqualTo( 42L );

         // CRASH SIMULATION: close without commitSync()
         // In production: an uncaught exception in @Transactional causes rollback,
         // and the Kafka offset is never committed because the listener method threw.
      } // ← consumer.close() — offset NOT committed

      // ── Attempt 2: New consumer with same group.id ────────────────────────
      // Resumes from the last committed offset (which is 0, since we never committed).
      // This is the AT-LEAST-ONCE redelivery.
      try ( KafkaConsumer< String, PaymentEvent > consumer2 = buildConsumer( broker, GROUP ) ) {
         consumer2.subscribe( List.of( TOPIC ) );
         ConsumerRecords< String, PaymentEvent > records = consumer2.poll( Duration.ofSeconds( 5 ) );

         // Same message re-delivered — this is at-least-once semantics
         assertThat( records.count() ).isEqualTo( 1 );
         assertThat( records.iterator().next().value().orderId() ).isEqualTo( 42L );

         // NOW commit — processing completed successfully
         consumer2.commitSync();
      }
   }

   // ── D2. Committed Offsets — No Re-Delivery ────────────────────────────────

   /**
    * KAFKA MECHANISM: Committed offsets prevent re-delivery
    * <p>
    * After a consumer calls commitSync(), the broker records the offset as
    * "consumed" for that group. Subsequent consumers in the same group start
    * AFTER the committed offset — the message is not re-delivered.
    * <p>
    * This test is the "happy path" counterpart to the crash test above.
    */
   @Test
   void afterSuccessfulCommit_messageIsNotRedelivered() {
      publishEvent( broker, 43L );

      // ── Attempt 1: Read AND commit ────────────────────────────────────────
      try ( KafkaConsumer< String, PaymentEvent > consumer = buildConsumer( broker, GROUP ) ) {
         consumer.subscribe( List.of( TOPIC ) );
         ConsumerRecords< String, PaymentEvent > records = consumer.poll( Duration.ofSeconds( 5 ) );

         assertThat( records.count() ).isEqualTo( 1 );
         consumer.commitSync(); // ← successful commit
      }

      // ── Attempt 2: Same group — should NOT re-read ────────────────────────
      try ( KafkaConsumer< String, PaymentEvent > consumer2 = buildConsumer( broker, GROUP ) ) {
         consumer2.subscribe( List.of( TOPIC ) );
         ConsumerRecords< String, PaymentEvent > records = consumer2.poll( Duration.ofSeconds( 3 ) );

         // Offset was committed — message is not replayed
         assertThat( records.count() ).isZero();
      }
   }

   // ── D3. Auto-Commit vs Manual Commit Comparison ───────────────────────────

   /**
    * KAFKA MECHANISM: enable.auto.commit=true (at-most-once risk)
    * <p>
    * With auto-commit, the consumer periodically commits the offset regardless
    * of whether processing succeeded. If the service crashes AFTER the auto-commit
    * but BEFORE the DB write completes, the message is LOST — at-most-once.
    * <p>
    * Timeline with auto-commit (interval=5s):
    * t=0s  - message polled
    * t=5s  - auto-commit fires (offset advanced)
    * t=6s  - service crashes during DB write
    * t=7s  - service restarts, resumes AFTER committed offset → message LOST
    * <p>
    * The order-service disables auto-commit (enable-auto-commit=false) and
    * relies on Spring Kafka's AckMode.BATCH to commit after each successful batch.
    * This gives at-least-once semantics.
    */
   @Test
   void autoCommit_canCauseAtMostOnce_messageNotRedeliveredAfterCrash() {
      publishEvent( broker, 44L );

      // Consumer with AUTO-COMMIT enabled (the dangerous setting)
      Map< String, Object > props = KafkaTestUtils.consumerProps( GROUP + "-auto", "true", broker );
      props.put( ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100 ); // commit every 100ms
      props.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true );
      props.put( JsonDeserializer.TRUSTED_PACKAGES, "dev.agasen.api.event" );
      props.put( JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentEvent.class.getName() );

      try ( KafkaConsumer< String, PaymentEvent > consumer =
               new KafkaConsumer<>( props, new StringDeserializer(), new JsonDeserializer<>( PaymentEvent.class ) ) ) {
         consumer.subscribe( List.of( TOPIC ) );
         ConsumerRecords< String, PaymentEvent > records = consumer.poll( Duration.ofSeconds( 5 ) );

         assertThat( records.count() ).isEqualTo( 1 );

         // Simulate: auto-commit fires here, then crash before DB write
         try {
            Thread.sleep( 200 );
         } catch ( InterruptedException ignored ) {
         }
         // crash simulation — no explicit commit, but auto-commit already ran
      }

      // Restart with same group — message is NOT re-delivered because auto-commit already fired
      // This is the AT-MOST-ONCE risk: the message is gone even though processing failed
      try ( KafkaConsumer< String, PaymentEvent > consumer2 =
               new KafkaConsumer<>( props, new StringDeserializer(), new JsonDeserializer<>( PaymentEvent.class ) ) ) {
         consumer2.subscribe( List.of( TOPIC ) );
         ConsumerRecords< String, PaymentEvent > records = consumer2.poll( Duration.ofSeconds( 3 ) );

         // Message is lost — at-most-once delivered
         // This is why the order-service uses enable-auto-commit=false
         assertThat( records.count() ).isZero();
      }
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private void publishEvent( EmbeddedKafkaBroker broker, long orderId ) {
      Map< String, Object > producerProps = Map.of(
         ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString(),
         ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
         ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
      );

      PaymentEvent event = new PaymentEvent( orderId, orderId * 10, "user-test", BigDecimal.TEN, "CAPTURED", null );

      try ( org.apache.kafka.clients.producer.KafkaProducer< String, PaymentEvent > producer =
               new org.apache.kafka.clients.producer.KafkaProducer<>( producerProps ) ) {
         producer.send( new ProducerRecord<>( TOPIC, String.valueOf( orderId ), event ) );
         producer.flush();
      }
   }

   private KafkaConsumer< String, PaymentEvent > buildConsumer( EmbeddedKafkaBroker broker, String groupId ) {
      Map< String, Object > props = KafkaTestUtils.consumerProps( groupId, "false", broker );
      props.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false );
      props.put( JsonDeserializer.TRUSTED_PACKAGES, "dev.agasen.api.event" );
      props.put( JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentEvent.class.getName() );
      return new KafkaConsumer<>( props, new StringDeserializer(), new JsonDeserializer<>( PaymentEvent.class ) );
   }
}
