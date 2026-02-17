package dev.agasen.core.user;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

//@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import( BaseIntegrationTest.NoSecurityConfig.class )
public abstract class BaseIntegrationTest {

   @Autowired
   protected MockMvc mockMvc;

   public static GenericContainer< ? > redis;
   public static PostgreSQLContainer< ? > postgres;

   @BeforeAll
   static void setup() {
      postgres = new PostgreSQLContainer<>( "postgres:17-alpine" )
            .withDatabaseName( "test-db" )
            .withUsername( "user" )
            .withPassword( "password" )
            .withCreateContainerCmdModifier( cmd -> {
               cmd.getHostConfig().withPortBindings(
                     new PortBinding( Ports.Binding.bindPort( 5555 ), new ExposedPort( 5432 ) )
               );
            } )
            .withReuse( true );

      redis = new RedisContainer( DockerImageName.parse( "redis:6.0.3" ) )
            .withExposedPorts( 6379 )
            .withReuse( true );

      postgres.start();
      redis.start();
   }

   @DynamicPropertySource
   public static void setProperties( DynamicPropertyRegistry registry ) {
      registry.add( "spring.datasource.url", () -> postgres.getJdbcUrl() );
      registry.add( "spring.datasource.username", () -> postgres.getUsername() );
      registry.add( "spring.datasource.password", () -> postgres.getPassword() );

      registry.add( "spring.redis.host", () -> redis.getHost() );
      registry.add( "spring.redis.port", () -> redis.getMappedPort( 6379 ) );
   }

   @TestConfiguration
   public static class NoSecurityConfig {
      @Bean
      public SecurityFilterChain filterChain( HttpSecurity http ) throws Exception {
         http.csrf().disable()
               .authorizeHttpRequests().anyRequest().permitAll();
         return http.build();
      }
   }
}
