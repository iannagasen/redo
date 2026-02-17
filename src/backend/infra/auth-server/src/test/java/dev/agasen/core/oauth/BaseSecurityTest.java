package dev.agasen.core.oauth;

import dev.agasen.common.utility.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.logging.Logger;

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
@ComponentScan( basePackages = { "dev.agasen.common", "dev.agasen.core" } )
public class BaseSecurityTest {

   public static final String CLIENT_ID = "test-id";
   public static final String CLIENT_SECRET = "test-secret";
   public static final String RESULT_INVALID_CLIENT = "invalid_client";
   static final Logger log = Logger.getLogger( BaseSecurityTest.class.getName() );

   @Autowired
   protected MockMvc mockMvc;

   @Autowired
   protected JsonHelper jsonHelper;

   @LocalServerPort
   protected int port;

}
