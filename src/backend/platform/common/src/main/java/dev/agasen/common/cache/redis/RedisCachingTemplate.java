package dev.agasen.common.cache.redis;

import dev.agasen.common.cache.CachingTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class RedisCachingTemplate< KEY, VALUE > implements CachingTemplate< KEY, VALUE > {

   private final RedisTemplate< KEY, VALUE > redisTemplate;

   public RedisCachingTemplate( RedisTemplate< KEY, VALUE > redisTemplate ) {
      this.redisTemplate = redisTemplate;
   }

   @Override
   public VALUE get( KEY key ) {
      return redisTemplate.opsForValue().get( key );
   }

   @Override
   public void set( KEY key, VALUE value ) {
      redisTemplate.opsForValue().set( key, value );
   }

   @Override
   public void set( KEY key, VALUE value, Duration duration ) {
      redisTemplate.opsForValue().set( key, value, duration );
   }

   @Override
   public void delete( KEY key ) {
      redisTemplate.delete( key );
   }

   @Override
   public boolean contains( KEY key ) {
      return redisTemplate.hasKey( key );
   }
}
