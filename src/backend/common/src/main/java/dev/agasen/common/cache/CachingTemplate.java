package dev.agasen.common.cache;

import java.time.Duration;

public interface CachingTemplate< KEY, VALUE > {

   VALUE get( KEY key );

   void set( KEY key, VALUE value );

   void set( KEY key, VALUE value, Duration duration );

   void delete( KEY key );

   boolean contains( KEY key );

}
