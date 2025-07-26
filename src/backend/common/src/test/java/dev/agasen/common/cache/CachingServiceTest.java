package dev.agasen.common.cache;

import dev.agasen.common.cache.redis.RedisCachingTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith( SpringExtension.class )
@ContextConfiguration( classes = CachingServiceTest.TestRedisConfig.class )
class CachingServiceTest {

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

   @Test
   void shouldWriteAndReadFromCache() {
      cachingServiceSUT.cacheValue( "testKey", "testValue" );

      Optional< String > result = cachingServiceSUT.find( "testKey" );

      assertThat( result ).isPresent().contains( "testValue" );
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
         return new CachingService<>( new RedisCachingTemplate<>( template ) ); // your implementation
      }
   }
}
