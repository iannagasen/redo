package dev.agasen.storefront;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping( "/api/auth" )
public class AuthController {

   private final RestClient restClient;

   private final String clientId = "angular-client";
   private final String clientSecret = "angular-secret";
   private final String redirectUri = "http://localhost:4200/login/callback";

   public AuthController() {
      this.restClient = RestClient.create();
   }

   @GetMapping( "/login" )
   public void login( HttpServletResponse response ) throws IOException {
      // ports 8080 - oauth, 7001 - this bff server
      // should be dynamic
      String authorizeUrl = "http://localhost:8080/oauth2/authorize" +
                            "?client_id=angular-client" +
                            "&response_type=code" +
                            "&redirect_uri=http://localhost:7001/api/auth/callback" +
                            "&scope=openid profile";
      // send redirect to the client
      response.sendRedirect( authorizeUrl );
   }

   @GetMapping( "/callback" )
   public void callback( @RequestParam( value = "code" ) String code, HttpServletResponse response ) throws IOException {        // Exchange code for tokens
      Map< String, Object > tokens = restClient.post()
            .uri( "/oauth2/token" )
            .contentType( MediaType.APPLICATION_FORM_URLENCODED )
            .body( "grant_type=authorization_code" +
                   "&code=" + code +
                   "&redirect_uri=" + redirectUri +
                   "&client_id=" + clientId +
                   "&client_secret=" + clientSecret )
            .retrieve()
            .body( new ParameterizedTypeReference< Map< String, Object > >() {
            } );

      String accessToken = ( String ) tokens.get( "access_token" );

      // Store token in HttpOnly cookie
      ResponseCookie cookie = ResponseCookie.from( "SESSION_TOKEN", accessToken )
            .httpOnly( true )
            .secure( false ) // set true in prod (HTTPS)
            .sameSite( "Strict" )
            .path( "/" )
            .build();

      response.addHeader( HttpHeaders.SET_COOKIE, cookie.toString() );

      // Redirect back to Angular SPA

      response.sendRedirect( "http://localhost:4200/" );
   }

}
