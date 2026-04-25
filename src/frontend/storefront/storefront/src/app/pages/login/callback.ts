import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OauthService } from '../../core/service/oauth-service';

@Component( {
  selector: 'app-callback',
  standalone: true,
  imports: [],
  template: `<p class="text-center py-20 text-gray-500">Logging you in...</p>`,
} )
export class LoginCallback implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: OauthService,
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get( 'code' );
    if ( code ) {
      this.auth.exchangeCodeForToken( code ).subscribe( tokens => {
        this.auth.storeTokens( tokens );
        this.router.navigate( [ '/dashboard' ] );
      } );
    }
  }
}
