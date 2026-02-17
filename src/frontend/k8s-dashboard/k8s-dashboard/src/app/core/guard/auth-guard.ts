import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { OauthService } from '../service/oauth-service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(OauthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};
