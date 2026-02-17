import { Component } from '@angular/core';
import { OauthService } from '../../core/service/oauth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100">
      <div class="w-full max-w-sm bg-white rounded-2xl shadow-lg p-8 text-center">
        <div class="mb-6">
          <h1 class="text-2xl font-bold text-gray-800">K8s Dashboard</h1>
          <p class="text-gray-500 text-sm mt-1">Sign in to manage your cluster</p>
        </div>

        <button type="button"
                (click)="login()"
                class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 px-4 rounded-xl transition">
          Sign in with Company Account
        </button>
      </div>
    </div>
  `,
})
export class Login {

  constructor(private auth: OauthService) {
  }

  login() {
    this.auth.login();
  }
}
