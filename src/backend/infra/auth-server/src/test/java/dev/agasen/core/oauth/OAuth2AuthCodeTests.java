package dev.agasen.core.oauth;

import org.junit.jupiter.api.Test;

import static dev.agasen.core.oauth.OAuth2Endpoints.AUTHORIZE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class OAuth2AuthCodeTests extends BaseSecurityTest {

   @Test
   public void testAuthorizationEndpoint_ValidRequest() throws Exception {
      mockMvc.perform( get( AUTHORIZE )
                  .param( "response_type", "code" )
                  .param( "client_id", CLIENT_ID )
                  .param( "redirect_uri", "http://localhost:8080/someshit" )
                  .param( "scope", "read" )
                  .param( "state", "test-state" ) )
            .andDo( print() );
   }
}
