import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

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
    </nav>
  `,
})
export class Sidebar {
  links = [
    { path: '/', label: 'Overview', exact: true },
    { path: '/pods', label: 'Pods', exact: false },
    { path: '/deployments', label: 'Deployments', exact: false },
    { path: '/services', label: 'Services', exact: false },
    { path: '/configmaps', label: 'ConfigMaps', exact: false },
  ];
}
