package dev.agasen.common.cache;

import dev.agasen.common.cache.redis.RedisCachingTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import redis.embedded.RedisServer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith( SpringExtension.class )
@ContextConfiguration( classes = CachingServiceTest.TestRedisConfig.class )
public class CachingServiceTest {

   private static RedisServer redisServer;

   @Autowired
   RedisTemplate< String, String > redisTemplate;

   @Autowired
   CachingService< String, String > cachingServiceSUT;

   @BeforeAll
   static void startRedis() {
      redisServer = new RedisServer( 6379 );
      redisServer.start();
   }

   @AfterAll
   static void stopRedis() {
      redisServer.stop();
   }

   @BeforeEach
   void setUp() {
      redisTemplate.delete( redisTemplate.keys( "*" ) );
   }

   @Test
   void shouldWriteAndReadFromCache() {
      cachingServiceSUT.cacheValue( "testKey", "testValue" );

      Optional< String > result = cachingServiceSUT.find( "testKey" );

      assertEquals( "testValue", result.get() );
      assertEquals( 1, redisTemplate.keys( "*" ).size() );
   }

   @Test
   void shouldDeleteFromCache() {
      cachingServiceSUT.cacheValue( "testKey", "testValue" );
      assertEquals( "testValue", cachingServiceSUT.find( "testKey" ).get() );
      assertEquals( 1, redisTemplate.keys( "*" ).size() );

      cachingServiceSUT.evict( "testKey" );
      assertTrue( cachingServiceSUT.find( "testKey" ).isEmpty() );
      assertEquals( 0, redisTemplate.keys( "*" ).size() );
   }

   @Test
   void shouldWriteWithExpiration() {
      Duration tenMinutes = Duration.of( 10, ChronoUnit.MINUTES );
      Long tenMinutesInSeconds = tenMinutes.toSeconds();

      cachingServiceSUT.cacheValue( "testKey2", "testValue2", tenMinutes );

      String result = cachingServiceSUT.find( "testKey2" ).get();

      assertEquals( "testValue2", result );
      assertEquals( 1, redisTemplate.keys( "*" ).size() );
      assertEquals( tenMinutesInSeconds, redisTemplate.getExpire( "testKey2" ) );
   }

   @Test
   void shouldOverrideValue() {
      String key = "testKey";

      cachingServiceSUT.cacheValue( key, "testValue" );
      assertEquals( "testValue", cachingServiceSUT.find( key ).get() );

      cachingServiceSUT.cacheValue( key, "testValue2" );
      assertEquals( "testValue2", cachingServiceSUT.find( key ).get() );
      assertEquals( 1, redisTemplate.keys( "*" ).size() );
   }


   @TestConfiguration
   static class TestRedisConfig {

      @Bean
      public RedisConnectionFactory redisConnectionFactory() {
         return new LettuceConnectionFactory( "localhost", 6379 );
      }

      @Bean
      public RedisTemplate< String, String > redisTemplate( RedisConnectionFactory factory ) {
         RedisTemplate< String, String > template = new RedisTemplate<>();
         template.setConnectionFactory( factory );
         template.setKeySerializer( new StringRedisSerializer() );
         template.setValueSerializer( new StringRedisSerializer() );
         return template;
      }

      @Bean
      public CachingService< String, String > cachingService( RedisTemplate< String, String > template ) {
         return new CachingService<>( new RedisCachingTemplate<>( template ) );
      }
   }
}
