import { Component } from '@angular/core';
import { OauthService } from '../../core/service/oauth-service';

@Component( {
  selector: 'app-login',
  standalone: true,
  imports: [],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100">
      <div class="w-full max-w-md bg-white rounded-2xl shadow-lg p-8">
        <!-- Logo / Title -->
        <div class="text-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">Welcome back</h1>
          <p class="text-gray-500 text-sm">Sign in to your account</p>
        </div>

        <!-- Placeholder form (no logic yet) -->
        <form class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700">Email</label>
            <input type="email"
                   class="mt-1 block w-full rounded-xl border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm p-3"
                   placeholder="you@example.com"/>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Password</label>
            <input type="password"
                   class="mt-1 block w-full rounded-xl border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm p-3"
                   placeholder="••••••••"/>
          </div>

          <!-- Login button -->
          <button type="button"
                  class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 px-4 rounded-xl transition">
            Sign in
          </button>
        </form>

        <!-- Divider -->
        <div class="my-6 flex items-center">
          <div class="flex-grow border-t border-gray-300"></div>
          <span class="px-3 text-sm text-gray-400">OR</span>
          <div class="flex-grow border-t border-gray-300"></div>
        </div>

        <!-- OAuth login buttons -->
        <div class="space-y-3">
          <button type="button"
                  class="w-full flex items-center justify-center gap-3 border border-gray-300 rounded-xl py-3 hover:bg-gray-50 transition">
            <img src="https://www.svgrepo.com/show/355037/google.svg" alt="Google" class="h-5 w-5"/>
            <span class="text-sm font-medium text-gray-700">Continue with Google</span>
          </button>

          <button type="button"
                  class="w-full flex items-center justify-center gap-3 border border-gray-300 rounded-xl py-3 hover:bg-gray-50 transition">
            <img src="https://www.svgrepo.com/show/521688/github.svg" alt="GitHub" class="h-5 w-5"/>
            <span class="text-sm font-medium text-gray-700">Continue with GitHub</span>
          </button>

          <!-- Company Account -->
          <button type="button"
                  (click)="login()"
                  class="w-full flex items-center justify-center gap-3 bg-indigo-600 text-white rounded-xl py-3 hover:bg-indigo-700 transition">
            <span class="text-sm font-medium">Sign in with Company Account</span>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: ``
} )
export class Login {

  constructor( private auth: OauthService ) {
  }

  login() {
    this.auth.login();
  }
}
