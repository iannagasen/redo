package dev.agasen.core.product.domain;

import dev.agasen.common.redis.CacheService;
import dev.agasen.common.redis.CachingService;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@CacheService
public record ProductCacheService(
      RedisTemplate< String, Product > productRedisTemplate
) implements CachingService< String, Product > {

   public static final String PREFIX = "product::";

   @Override
   public Product getByKey( String key ) {
      return productRedisTemplate.opsForValue().get( PREFIX + key );
   }

   @Override
   public void cacheValue( Product product ) {
      productRedisTemplate.opsForValue().set( PREFIX + product.id(), product, Duration.ofMinutes( 1 ) );
   }

}
