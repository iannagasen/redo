package dev.agasen.core.oauth;

import dev.agasen.common.utility.JsonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.logging.Logger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles( "test" )
//@ComponentScan( basePackages = { "dev.agasen.common", "dev.agasen.core" } )
public class SecurityTests {

   private static final Logger log = Logger.getLogger( SecurityTests.class.getName() );

   //   @Autowired
   MockMvc mockMvc;

   //   @Autowired
   JsonHelper jsonHelper;

   //   TODO: implement test methods below this line
   @Test
   public void testMissingGrantTypeReturnsError() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
            .with( httpBasic( "client-id", "secret" ) ) );
   }

   @Test
   public void testUnsupportedGrantTypeReturnsError() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
            .with( httpBasic( "client-id", "secret" ) )
            .param( "grant_type", "password" ) );
   }

   @Test
   public void testInvalidClientIdReturnsError() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
            .with( httpBasic( "invalid-client-id", "secret" ) )
            .param( "grant_type", "client_credentials" ) );
   }

   @Test
   public void testAccessTokenSuccess_withScopeValidation() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
            .with( httpBasic( "client-id", "secret" ) )
            .param( "grant_type", "client_credentials" )
            .param( "scope", "read write" ) );
   }

   @Test
   public void testAccessTokenExpiryMatchesConfiguration() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
            .with( httpBasic( "client-id", "secret" ) )
            .param( "grant_type", "client_credentials" ) );
   }


}
