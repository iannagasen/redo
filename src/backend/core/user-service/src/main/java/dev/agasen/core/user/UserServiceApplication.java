package dev.agasen.core.user;

import dev.agasen.platform.contracts.GlobalExceptionHandling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

}
