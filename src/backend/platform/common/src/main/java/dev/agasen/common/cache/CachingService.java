package dev.agasen.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachingService< KEY, VALUE > {

   private static final Logger logger = LoggerFactory.getLogger( CachingService.class );

   private final CachingTemplate< KEY, VALUE > template;

   private Duration defaultDuration;
   private Function< KEY, KEY > keyGenerator;

   public CachingService( CachingTemplate< KEY, VALUE > template ) {
      this.template = template;
   }

   public CachingService( CachingTemplate< KEY, VALUE > template, Duration defaultDuration, Function< KEY, KEY > keyGenerator ) {
      this.template = template;
      this.defaultDuration = defaultDuration;
      this.keyGenerator = keyGenerator;
   }

   public Optional< VALUE > find( KEY key ) {
      return Optional.ofNullable( template.get( calculateKey( key ) ) );
   }

   public void cacheValue( KEY key, VALUE value ) {
      doCacheValue( key, value, defaultDuration );
   }

   public void cacheValue( KEY key, VALUE value, Duration duration ) {
      doCacheValue( key, value, duration );
   }

   public void evict( KEY key ) {
      logger.debug( "Evicting value for key {}", key );
      template.delete( calculateKey( key ) );
   }

   public VALUE getCachedOrCompute( KEY key, Supplier< VALUE > fallbackLoader ) {
      Optional< VALUE > value = find( calculateKey( key ) );
      if ( value.isPresent() ) {
         VALUE found = value.get();
         logger.debug( "Value for key {} found: {}", key, found );
         return found;
      } else {
         VALUE fallback = fallbackLoader.get();
         logger.debug( "No Value for key {} found, using fallback value: {}", key, fallback );
         cacheValue( calculateKey( key ), fallback );
         return fallback;
      }
   }

   private KEY calculateKey( KEY key ) {
      return keyGenerator == null
         ? key
         : keyGenerator.apply( key );
   }

   private void doCacheValue( KEY key, VALUE value, Duration duration ) {
      var calculatedKey = calculateKey( key );

      if ( duration == null ) {
         logger.debug( "Caching value: {}, with key {} with no duration", value, calculatedKey );
         template.set( calculatedKey, value );
      } else {
         logger.debug( "Caching value: {}, with key {}, using default duration of {}", value, calculatedKey, duration );
         template.set( calculatedKey, value, duration );
      }
   }
}
