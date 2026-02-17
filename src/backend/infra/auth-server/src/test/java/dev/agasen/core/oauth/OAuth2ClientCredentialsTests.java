package dev.agasen.core.oauth;

import org.apache.tomcat.util.http.parser.Authorization;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OAuth2ClientCredentialsTests extends BaseSecurityTest {

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
      mockMvc.perform( post( OAuth2Endpoints.POST_ACCESS_TOKEN )
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
      mockMvc.perform( post( OAuth2Endpoints.POST_ACCESS_TOKEN )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, BaseSecurityTest.CLIENT_SECRET ) )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.access_token" ).exists() )
            .andExpect( jsonPath( "$.expires_in" ).exists() )
            .andExpect( jsonPath( "$.token_type" ).value( "Bearer" ) )
            .andDo( print() );
   }

   @Test
   public void testClientAuthenticationFailsWithInvalidSecret() throws Exception {
      mockMvc.perform( post( "/oauth2/token" )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, "invalid_secret" ) )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isUnauthorized() )
            .andExpect( jsonPath( "$.error" ).value( BaseSecurityTest.RESULT_INVALID_CLIENT ) )
            .andDo( print() );
   }
}
