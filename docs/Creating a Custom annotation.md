### Creating a custom annotation - @CacheService

Step 1: Define the Annotation

```java

@Target( { ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
@Component  // So Spring still picks it up as a bean
public @interface CacheService {
   String value() default "";
}
```

### Explanation:

`@Target(TYPE)` means it can be used on classes.
`@Retention(RUNTIME)` means it's available at runtime (so Spring can use it).
`@Component` is meta-annotated to make Spring register it as a bean.
`@Documented` makes it appear in Javadoc.