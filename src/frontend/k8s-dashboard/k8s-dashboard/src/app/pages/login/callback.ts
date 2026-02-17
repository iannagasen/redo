import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OauthService } from '../../core/service/oauth-service';

@Component({
  selector: 'app-callback',
  standalone: true,
  imports: [],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100">
      <p class="text-gray-600">Logging you in...</p>
    </div>
  `,
})
export class LoginCallback implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: OauthService
  ) {
  }

  ngOnInit() {
    const code = this.route.snapshot.queryParamMap.get('code');
    if (code) {
      this.auth.exchangeCodeForToken(code).subscribe(tokens => {
        this.auth.storeTokens(tokens);
        this.router.navigate(['/']);
      });
    }
  }
}
