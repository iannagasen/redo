import { Component } from '@angular/core';
import { OauthService } from '../login/oauth-service';
import { Router } from '@angular/router';

@Component( {
  selector: 'app-dashboard',
  imports: [],
  template: `
    <p>
      dashboard works!
    </p>
    <button (click)="logout()">Logout</button>
  `,
  styles: ``
} )
export class Dashboard {

  constructor( private auth: OauthService, private router: Router ) {
  }

  logout() {
    this.auth.logout();
    this.router.navigate( [ '/login' ] );
  }
}
