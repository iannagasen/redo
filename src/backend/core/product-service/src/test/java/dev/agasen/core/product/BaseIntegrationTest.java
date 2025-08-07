package dev.agasen.core.product;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
public class BaseIntegrationTest {

   @Container
   public static GenericContainer< ? > redis = new GenericContainer<>( DockerImageName.parse( "redis:6.0.3" ) )
         .withExposedPorts( 6379 )
         .withReuse( true );

   @Container
   public static PostgreSQLContainer< ? > postgres = new PostgreSQLContainer<>( "postgres:17-alpine" )
         .withDatabaseName( "test-db" )
         .withUsername( "user" )
         .withPassword( "password" );


   @DynamicPropertySource
   public static void setProperties( DynamicPropertyRegistry registry ) {
      registry.add( "spring.datasource.url", postgres::getJdbcUrl );
      registry.add( "spring.datasource.username", postgres::getUsername );
      registry.add( "spring.datasource.password", postgres::getPassword );

      registry.add( "spring.redis.host", redis::getHost );
      registry.add( "spring.redis.port", () -> redis.getMappedPort( 6379 ) );
   }

}