import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OauthService } from '../service/oauth-service';

/**
 * Attaches the Bearer token to every outbound request.
 * Skips the OAuth token-exchange endpoint to avoid circular dependency.
 */
export const authInterceptor: HttpInterceptorFn = ( req, next ) => {
  const oauth = inject( OauthService );
  const token = oauth.getAccessToken();

  if ( !token || req.url.includes( '/oauth2/token' ) ) {
    return next( req );
  }

  return next( req.clone( { setHeaders: { Authorization: `Bearer ${ token }` } } ) );
};
