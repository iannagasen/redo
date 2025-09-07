import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export const loggingInterceptor: HttpInterceptorFn = ( req, next ) => {
  console.log( 'HTTP Request:', {
    url: req.url,
    method: req.method,
    headers: req.headers,
    body: req.body
  } );

  return next( req ).pipe(
    tap( {
      next: ( event ) => {
        if ( event instanceof HttpResponse ) {
          console.log( '⬅️ HTTP Response:', {
            url: event.url,
            status: event.status,
            body: event.body
          } );
        }
      },
      error: ( err ) => {
        console.error( '❌ HTTP Error:', err );
      }
    } )
  );
};
