import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { loggingInterceptor } from './core/interceptor/logging-interceptor';
import { authInterceptor } from './core/interceptor/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter( routes ),
    // authInterceptor runs first so every subsequent request already has the Bearer token.
    provideHttpClient( withFetch(), withInterceptors( [ authInterceptor, loggingInterceptor ] ) )
  ]
};
