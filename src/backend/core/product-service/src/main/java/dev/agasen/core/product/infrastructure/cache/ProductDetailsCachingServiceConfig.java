package dev.agasen.core.product.infrastructure.cache;

import dev.agasen.api.core.product.product.ProductDetails;
import dev.agasen.common.cache.CachingService;
import dev.agasen.common.cache.CachingTemplate;
import dev.agasen.common.cache.redis.RedisCachingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@Configuration
public class ProductDetailsCachingServiceConfig {

   public static final String KEY_PREFIX = "product:";

   @Bean
   public CachingTemplate< String, ProductDetails > configureRedisCachingTemplate( RedisTemplate< String, ProductDetails > template ) {
      return new RedisCachingTemplate<>( template );
   }

   @Bean
   public CachingService< String, ProductDetails > configureProductCachingService( CachingTemplate< String, ProductDetails > template ) {
      return new CachingService<>(
         template,
         Duration.ofMinutes( 10 ),
         key -> KEY_PREFIX + key
      );
   }

   @Bean
   public RedisTemplate< String, ProductDetails > redisTemplate( RedisConnectionFactory connectionFactory ) {
      RedisTemplate< String, ProductDetails > template = new RedisTemplate<>();
      template.setConnectionFactory( connectionFactory );
      template.setKeySerializer( new StringRedisSerializer() );
      template.setValueSerializer( new GenericJacksonJsonRedisSerializer( typingObjectMapper() ) );
      return template;
   }

   private ObjectMapper typingObjectMapper() {
      return JsonMapper.builder()
         .activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
               .allowIfSubType( "dev.agasen." )
               .allowIfSubType( "java." )
               .build(),
            DefaultTyping.NON_FINAL
         )
         .build();
   }
}
