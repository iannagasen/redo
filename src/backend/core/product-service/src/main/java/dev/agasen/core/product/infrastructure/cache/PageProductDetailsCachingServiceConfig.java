package dev.agasen.core.product.infrastructure.cache;

import dev.agasen.api.product.product.ProductDetails;
import dev.agasen.common.cache.CachingService;
import dev.agasen.common.cache.CachingTemplate;
import dev.agasen.common.cache.redis.RedisCachingTemplate;
import dev.agasen.common.pagination.PagedResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class PageProductDetailsCachingServiceConfig {

   public static final String KEY_PREFIX = "page-product:";

   @Bean
   public CachingTemplate< String, PagedResult< ProductDetails > > configureRedisProductPageCachingTemplate( RedisTemplate< String, PagedResult< ProductDetails > > template ) {
      return new RedisCachingTemplate<>( template );
   }

   @Bean
   public CachingService< String, PagedResult< ProductDetails > > configureProductPageCachingService( CachingTemplate< String, PagedResult< ProductDetails > > template ) {
      return new CachingService<>(
         template,
         Duration.ofMinutes( 10 ),
         key -> KEY_PREFIX + key
      );
   }

   @Bean
   public RedisTemplate< String, PagedResult< ProductDetails > > productPageRedisTemplate( RedisConnectionFactory connectionFactory ) {
      RedisTemplate< String, PagedResult< ProductDetails > > template = new RedisTemplate<>();
      template.setConnectionFactory( connectionFactory );
      template.setKeySerializer( new StringRedisSerializer() );
      template.setValueSerializer( new GenericJackson2JsonRedisSerializer() );
      return template;
   }
}
