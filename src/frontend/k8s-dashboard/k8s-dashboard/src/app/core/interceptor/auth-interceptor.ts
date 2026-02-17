import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OauthService } from '../service/oauth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Don't attach token to auth server requests (token exchange, etc.)
  if (req.url.includes('/oauth2/')) {
    return next(req);
  }

  const oauthService = inject(OauthService);
  const token = oauthService.getAccessToken();

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  return next(req);
};
