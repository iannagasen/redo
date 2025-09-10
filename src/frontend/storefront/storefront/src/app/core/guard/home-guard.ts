import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { OauthService } from '../service/oauth-service';

export const homeGuard: CanActivateFn = ( route, state ) => {
  const authService = inject( OauthService );
  const router = inject( Router );

  if ( authService.isLoggedIn() ) {
    return true;
  } else {
    // redirect to login if not authenticated
    return router.createUrlTree( [ '/login' ], {
      queryParams: { returnUrl: state.url }
    } )
  }
};
