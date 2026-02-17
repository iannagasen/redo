import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './components/layout/sidebar';
import { OauthService } from './core/service/oauth-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Sidebar],
  template: `
    @if (auth.isLoggedIn()) {
      <div class="flex h-screen bg-gray-100">
        <app-sidebar />
        <main class="flex-1 overflow-auto p-6">
          <router-outlet />
        </main>
      </div>
    } @else {
      <router-outlet />
    }
  `,
})
export class App {
  auth = inject(OauthService);
}
