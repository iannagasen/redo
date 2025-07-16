package dev.agasen.core.oauth;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTests {

   public static final String POST_ACCESS_TOKEN = "/oauth2/token";
   public static final String GET_WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";

   @Autowired
   private MockMvc mockMvc;

   @Test
   public void testGetAccessTokenFail() throws Exception {
      mockMvc.perform( post( POST_ACCESS_TOKEN )
                  .param( "client_id", "test" )
                  .param( "client_secret", "test" )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isUnauthorized() )
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

}
