package dev.agasen.common.redis;

import java.util.Optional;
import java.util.function.Supplier;

public interface CachingService< KEY, VALUE > {

   VALUE getByKey( KEY key );

   void cacheValue( VALUE value );

   default Optional< VALUE > findByKey( KEY key ) {
      return Optional.ofNullable( getByKey( key ) );
   }

   default void cacheValueIfPresent( VALUE value ) {
      if ( value != null ) {
         cacheValue( value );
      }
   }

   default VALUE getCachedOrCompute( KEY key, Supplier< VALUE > supplier ) {
      VALUE value = getByKey( key );
      if ( value != null ) {
         return value;
      }

      VALUE fallback = supplier.get();
      if ( fallback != null ) {
         cacheValue( fallback );
      }
      return fallback;
   }

}
