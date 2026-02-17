import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { OauthService } from '../../core/service/oauth-service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="w-56 bg-gray-900 text-gray-300 flex flex-col h-full shrink-0">
      <div class="p-4 text-lg font-bold text-white border-b border-gray-700">
        K8s Dashboard
      </div>
      <ul class="flex-1 py-2">
        @for (link of links; track link.path) {
          <li>
            <a
              [routerLink]="link.path"
              routerLinkActive="bg-gray-700 text-white"
              [routerLinkActiveOptions]="{ exact: link.exact }"
              class="block px-4 py-2.5 hover:bg-gray-800 hover:text-white transition-colors"
            >
              {{ link.label }}
            </a>
          </li>
        }
      </ul>
      <div class="border-t border-gray-700 p-4">
        <button
          (click)="logout()"
          class="w-full text-left px-2 py-2 text-sm text-gray-400 hover:text-white hover:bg-gray-800 rounded transition-colors"
        >
          Logout
        </button>
      </div>
    </nav>
  `,
})
export class Sidebar {
  private auth = inject(OauthService);
  private router = inject(Router);

  links = [
    { path: '/overview', label: 'Overview', exact: true },
    { path: '/pods', label: 'Pods', exact: false },
    { path: '/deployments', label: 'Deployments', exact: false },
    { path: '/services', label: 'Services', exact: false },
    { path: '/configmaps', label: 'ConfigMaps', exact: false },
  ];

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
