package dev.agasen.core.product;

import dev.agasen.common.cache.CachingService;
import dev.agasen.common.cache.CachingTemplate;
import dev.agasen.common.cache.redis.RedisCachingTemplate;
import dev.agasen.core.product.domain.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CachingServiceConfig {

   @Bean
   public CachingTemplate< String, Product > configureRedisCachingTemplate( RedisTemplate< String, Product > template ) {
      return new RedisCachingTemplate<>( template );
   }

   @Bean
   public CachingService< String, Product > configureProductCachingService( CachingTemplate< String, Product > template ) {
      return new CachingService<>(
            template,
            Duration.ofMinutes( 10 ),
            key -> "product:" + key
      );
   }

   @Bean
   public RedisTemplate< String, Product > redisTemplate( RedisConnectionFactory connectionFactory ) {
      RedisTemplate< String, Product > template = new RedisTemplate<>();
      template.setConnectionFactory( connectionFactory );
      template.setKeySerializer( new StringRedisSerializer() );
      template.setValueSerializer( new GenericJackson2JsonRedisSerializer() );
      return template;
   }
}
