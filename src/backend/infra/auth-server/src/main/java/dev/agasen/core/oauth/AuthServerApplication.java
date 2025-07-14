package dev.agasen.core.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan( "dev.agasen.core.oauth" )
public class AuthServerApplication {

   public static void main( String[] args ) {
      SpringApplication.run( AuthServerApplication.class, args );
   }

}
