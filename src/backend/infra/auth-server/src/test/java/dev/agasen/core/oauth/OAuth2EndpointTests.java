package dev.agasen.core.oauth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OAuth2EndpointTests extends BaseSecurityTest {

   @Test
   public void testWellKnownEndpoint() throws Exception {
      mockMvc.perform( get( OAuth2Endpoints.GET_WELL_KNOWN_ENDPOINT ) )
            .andExpect( status().isOk() )
            .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
            .andExpect( jsonPath( "$.issuer" ).value( "http://localhost:8080" ) )
            .andExpect( jsonPath( "$.authorization_endpoint" ).exists() )
            .andExpect( jsonPath( "$.token_endpoint" ).exists() )
            .andDo( print() );
   }

   @Test
   public void testTokenIntrospectionEndpoint() throws Exception {
      String responseAsString = mockMvc.perform( post( OAuth2Endpoints.POST_ACCESS_TOKEN )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, BaseSecurityTest.CLIENT_SECRET ) )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isOk() )
            .andReturn()
            .getResponse().getContentAsString();

      String accessToken = JsonPath.read( responseAsString, "$.access_token" );

      var result = mockMvc.perform( post( OAuth2Endpoints.OAUTH_2_INTROSPECT )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, BaseSecurityTest.CLIENT_SECRET ) )
                  .param( "token", accessToken ) )
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.active" ).value( true ) )
            .andExpect( jsonPath( "$.client_id" ).value( BaseSecurityTest.CLIENT_ID ) )
            .andExpect( jsonPath( "$.token_type" ).value( "Bearer" ) )
            .andDo( print() )
            .andReturn().getResponse().getContentAsString();

      System.out.println( jsonHelper.prettyPrint( result ) );
   }


   @Test
   public void testTokenRevocationEndpoint() throws Exception {
      // get access token
      String responseAsString = mockMvc.perform( post( OAuth2Endpoints.POST_ACCESS_TOKEN )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, BaseSecurityTest.CLIENT_SECRET ) )
                  .param( "grant_type", "client_credentials" ) )
            .andExpect( status().isOk() )
            .andReturn()
            .getResponse().getContentAsString();

      String accessToken = JsonPath.read( responseAsString, "$.access_token" );

      log.info( "access token: " + accessToken );

      // revoke access token
      var revokeResult = mockMvc.perform( post( OAuth2Endpoints.OAUTH_2_REVOKE )
                  .param( "token", accessToken )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, BaseSecurityTest.CLIENT_SECRET ) ) )
            .andDo( print() )
            .andExpect( status().isOk() )
            .andReturn().getResponse().getContentAsString();

      log.info( jsonHelper.prettyPrint( revokeResult ) );

      // introspect accesstoken if its still valid, should be falsie after revoking
      var introspect = mockMvc.perform( post( OAuth2Endpoints.OAUTH_2_INTROSPECT )
                  .with( httpBasic( BaseSecurityTest.CLIENT_ID, BaseSecurityTest.CLIENT_SECRET ) )
                  .param( "token", accessToken ) )
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.active" ).value( false ) )
            .andDo( print() )
            .andReturn().getResponse().getContentAsString();

      log.info( "Introspection: \n" + jsonHelper.prettyPrint( introspect ) );
   }


   @Test
   public void testJwksEndpoint() throws Exception {
      // endpoint should be available without security
      // TODO Q: Why is this public? because the result keys are public keys
      var res = mockMvc.perform( get( OAuth2Endpoints.WELL_KNOWN_JWKS_JSON ) )
            .andExpect( status().isOk() )
            .andDo( print() )
            .andExpect( jsonPath( "$.keys" ).exists() )
            .andReturn().getResponse().getContentAsString();

      log.info( jsonHelper.prettyPrint( res ) );
   }
}
