package dev.agasen.common.redis;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target( { ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
@Component
public @interface CacheService {
   String value() default "";
}
