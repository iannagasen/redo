package dev.agasen.core.cart.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepository {

   static final Duration TTL = Duration.ofDays( 30 );
   static final String KEY_PREFIX = "cart:";

   private final RedisTemplate<String, Cart> redisTemplate;

   public Optional<Cart> findByUserId( String userId ) {
      Cart cart = redisTemplate.opsForValue().get( KEY_PREFIX + userId );
      return Optional.ofNullable( cart );
   }

   public void save( String userId, Cart cart ) {
      redisTemplate.opsForValue().set( KEY_PREFIX + userId, cart, TTL );
   }

   public void delete( String userId ) {
      redisTemplate.delete( KEY_PREFIX + userId );
   }
}
