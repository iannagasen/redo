package dev.agasen.core.payment;

import dev.agasen.platform.contracts.GlobalExceptionHandling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(
   scanBasePackages = {
      "dev.agasen.core.payment"
   },
   scanBasePackageClasses = {
      GlobalExceptionHandling.class
   }
)
@EnableJpaAuditing
public class PaymentServiceApplication {

   public static void main( String[] args ) {
      SpringApplication.run( PaymentServiceApplication.class, args );
   }

}
