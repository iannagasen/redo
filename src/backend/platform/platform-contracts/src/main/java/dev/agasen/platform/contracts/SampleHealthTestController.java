package dev.agasen.platform.contracts;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleHealthTestController {

   @GetMapping( "/public/hello" )
   public String hello() {
      return "Hello World";
   }

}
