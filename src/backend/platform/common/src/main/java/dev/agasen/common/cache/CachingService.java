package dev.agasen.common.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachingService< KEY, VALUE > {

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
      if ( defaultDuration == null ) {
         template.set( calculateKey( key ), value );
      } else {
         template.set( calculateKey( key ), value, defaultDuration );
      }
   }

   public void cacheValue( KEY key, VALUE value, Duration duration ) {
      template.set( calculateKey( key ), value, duration );
   }

   public void evict( KEY key ) {
      template.delete( calculateKey( key ) );
   }

   public VALUE getCachedOrCompute( KEY key, Supplier< VALUE > fallbackLoader ) {
      Optional< VALUE > value = find( calculateKey( key ) );
      if ( value.isPresent() ) {
         return value.get();
      } else {
         VALUE fallback = fallbackLoader.get();
         cacheValue( calculateKey( key ), fallback );
         return fallback;
      }
   }

   private KEY calculateKey( KEY key ) {
      return keyGenerator == null
            ? key
            : keyGenerator.apply( key );
   }
}
