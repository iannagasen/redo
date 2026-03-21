package dev.agasen.core.oauth;

public class OAuth2Endpoints {

   public static final String AUTHORIZE = "/oauth2/authorize";

   public static final String POST_ACCESS_TOKEN = "/oauth2/token";
   public static final String GET_WELL_KNOWN_ENDPOINT = "/.well-known/openid-configuration";
   public static final String OAUTH_2_REVOKE = "/oauth2/revoke";
   public static final String OAUTH_2_INTROSPECT = "/oauth2/introspect";
   // see GET_WELL_KNOWN_ENDPOINT for list of endpoints
   public static final String WELL_KNOWN_JWKS_JSON = "/oauth2/jwks";
}
