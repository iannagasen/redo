import {Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {OauthService} from './oauth-service';

@Component({
  selector: 'app-callback',
  imports: [],
  template: `
    <p>
      Logging you in...
    </p>
  `,
  styles: ``
})
export class LoginCallback {

  constructor(private route: ActivatedRoute, private auth: OauthService) {
  }

  ngOnInit() {
    const code = this.route.snapshot.queryParamMap.get('code');
    console.log('Authorization code: ' + code);
    if (code) {
      this.auth.exchangeCodeForToken(code).subscribe(tokens => {
        this.auth.storeTokens(tokens);
        window.location.href = '/'
      })
    }
  }
}
