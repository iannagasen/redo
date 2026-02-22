package dev.agasen.core.cart.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.agasen.core.cart.domain.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

   @Bean
   public RedisTemplate<String, Cart> cartRedisTemplate( RedisConnectionFactory connectionFactory ) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule( new JavaTimeModule() );
      mapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );
      mapper.activateDefaultTyping(
         BasicPolymorphicTypeValidator.builder()
            .allowIfSubType( Object.class )
            .build(),
         ObjectMapper.DefaultTyping.NON_FINAL,
         JsonTypeInfo.As.PROPERTY
      );

      RedisTemplate<String, Cart> template = new RedisTemplate<>();
      template.setConnectionFactory( connectionFactory );
      template.setKeySerializer( new StringRedisSerializer() );
      template.setValueSerializer( new GenericJackson2JsonRedisSerializer( mapper ) );
      return template;
   }
}
