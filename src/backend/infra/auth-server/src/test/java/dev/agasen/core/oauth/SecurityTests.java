package dev.agasen.core.oauth;

import com.jayway.jsonpath.JsonPath;
import dev.agasen.common.utility.JsonHelper;
import org.apache.tomcat.util.http.parser.Authorization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
@ComponentScan( basePackages = { "dev.agasen.common", "dev.agasen.core" } )
public class SecurityTests {

   public static final String POST_ACCESS_TOKEN = "/oauth2/token";
   public static final String GET_WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";

   public static final String CLIENT_ID = "test-id";
   public static final String CLIENT_SECRET = "test-secret";

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private JsonHelper jsonHelper;

   ///  ## Request
   /// - HTTP Method = POST
   /// - Request URI = /oauth2/token
   /// - Parameters: `{client_id=[test], client_secret=[test], grant_type=[client_credentials]}`
   ///
   /// ## Response
   /// - Status = 401
   /// - Error message = null
   /// - Body = {"error":"invalid_client"}
   @Test
   public void testGetAccessTokenFail() throws Exception {
      mockMvc.perform( post( POST_ACCESS_TOKEN )
                  .param( "client_id", "test" )
                  .param( "client_secret", "test" )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isUnauthorized() )
            .andExpect( jsonPath( "$.error" ).value( "invalid_client" ) )
            .andDo( print() );
   }

   /// #### NOTE: the client uses Client Basic secret as Client Auth method
   /// #### the request will be different with {@see testGetAccessTokenFail()}
   ///
   /// ### Request
   /// - HTTP Method = POST
   /// - Request URI = /oauth2/token
   /// - Parameters = `{grant_type=[client_credentials]}`
   /// - Headers = [Authorization:"Basic cHJvZHVjdC1zZXJ2aWNlOnByb2R1Y3Qtc2VjcmV0"]
   /// - Body = null
   /// ### Response
   /// - MockHttpServletResponse:
   /// - Status = 200
   /// - Content type = application/json;charset=UTF-8
   /// - Body = {"access_token":"...","token_type":"Bearer","expires_in":1799}
   @Test
   public void testGetAccessTokenSuccess_usingClientSecretBasicForAuthentication() throws Exception {
      mockMvc.perform( post( POST_ACCESS_TOKEN )
                  .with( httpBasic( CLIENT_ID, CLIENT_SECRET ) )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.access_token" ).exists() )
            .andExpect( jsonPath( "$.expires_in" ).value( 1799 ) )
            .andExpect( jsonPath( "$.token_type" ).value( "Bearer" ) )
            .andDo( print() );
   }


   @Test
   public void testWellKnownEndpoint() throws Exception {
      mockMvc.perform( get( GET_WELL_KNOWN_ENDPOINT ) )
            .andExpect( status().isOk() )
            .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
            .andExpect( jsonPath( "$.issuer" ).value( "http://localhost:8080" ) )
            .andExpect( jsonPath( "$.authorization_endpoint" ).exists() )
            .andExpect( jsonPath( "$.token_endpoint" ).exists() )
            .andDo( print() );
   }

//   TODO: implement test methods below this line

   @Test
   public void testTokenIntrospectionEndpoint() throws Exception {
      String responseAsString = mockMvc.perform( post( POST_ACCESS_TOKEN )
                  .with( httpBasic( CLIENT_ID, CLIENT_SECRET ) )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isOk() )
            .andReturn()
            .getResponse().getContentAsString();

      String accessToken = JsonPath.read( responseAsString, "$.access_token" );

      var result = mockMvc.perform( post( "/oauth2/introspect" )
                  .with( httpBasic( CLIENT_ID, CLIENT_SECRET ) )
                  .param( "token", accessToken ) )
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.active" ).value( true ) )
            .andDo( print() )
            .andReturn().getResponse().getContentAsString();

      System.out.println( jsonHelper.prettyPrint( result ) );
   }

   @Test
   public void testTokenRevocationEndpoint() throws Exception {
      mockMvc.perform( post( "/oauth2/revoke" )
            .param( "token", "dummy_token" ) );
   }

   @Test
   public void testJwksEndpoint() throws Exception {
      mockMvc.perform( get( "/.well-known/jwks.json" ) );
   }

   @Test
   public void testClientAuthenticationFailsWithInvalidSecret() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
            .with( httpBasic( "valid-client-id", "invalid-secret" ) )
            .param( "grant_type", "client_credentials" ) );
   }

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
