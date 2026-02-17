package dev.agasen.core.user;

import dev.agasen.api.GlobalExceptionHandling;
import dev.agasen.api.user.user.UserCreationDetails;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@SpringBootApplication(
      scanBasePackages = {
            "dev.agasen.core.user"
      },
      scanBasePackageClasses = {
            GlobalExceptionHandling.class
      }

)
public class UserServiceApplication {

   public static void main( String[] args ) {
      SpringApplication.run( UserServiceApplication.class, args );
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

   @Bean
   public CommandLineRunner commandLineRunner( UserRestService userRestService ) {
      return args -> {
//         getUserCreationDetails().forEach( userRestController::createUser );
         // need to create permissions and roles first
      };
   }

   List< UserCreationDetails > getUserCreationDetails() {
      return List.of(
            new UserCreationDetails( "jdoe", "secret123", "jdoe@example.com", "John", "Doe", Set.of( "USER" ) ),
            new UserCreationDetails( "asmith", "password456", "asmith@example.com", "Alice", "Smith", Set.of( "USER", "ADMIN" ) ),
            new UserCreationDetails( "bjones", "qwerty789", "bjones@example.com", "Bob", "Jones", Set.of( "USER" ) ),
            new UserCreationDetails( "cwhite", "letmein321", "cwhite@example.com", "Carol", "White", Set.of( "MANAGER" ) ),
            new UserCreationDetails( "dking", "dragonpass", "dking@example.com", "David", "King", Set.of( "USER", "MODERATOR" ) )
      );
   }


}
