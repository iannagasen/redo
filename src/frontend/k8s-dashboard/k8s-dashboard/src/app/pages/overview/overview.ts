import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { K8sApiService } from '../../core/service/k8s-api-service';

@Component({
  selector: 'app-overview',
  template: `
    <h1 class="text-2xl font-bold text-gray-800 mb-6">Cluster Overview</h1>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      @for (card of cards(); track card.label) {
        <button
          (click)="navigate(card.route)"
          class="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow text-left cursor-pointer"
        >
          <div class="text-sm font-medium text-gray-500 uppercase tracking-wide">{{ card.label }}</div>
          <div class="mt-2 text-3xl font-bold text-gray-900">
            @if (card.loading) {
              <span class="text-gray-400">...</span>
            } @else {
              {{ card.count }}
            }
          </div>
        </button>
      }
    </div>
  `,
})
export class Overview implements OnInit {
  private api = inject(K8sApiService);
  private router = inject(Router);

  cards = signal([
    { label: 'Pods', count: 0, loading: true, route: '/pods' },
    { label: 'Deployments', count: 0, loading: true, route: '/deployments' },
    { label: 'Services', count: 0, loading: true, route: '/services' },
    { label: 'ConfigMaps', count: 0, loading: true, route: '/configmaps' },
  ]);

  ngOnInit() {
    this.api.getPods().subscribe(pods => this.updateCard(0, pods.length));
    this.api.getDeployments().subscribe(deps => this.updateCard(1, deps.length));
    this.api.getServices().subscribe(svcs => this.updateCard(2, svcs.length));
    this.api.getConfigMaps().subscribe(cms => this.updateCard(3, cms.length));
  }

  navigate(route: string) {
    this.router.navigate([route]);
  }

  private updateCard(index: number, count: number) {
    this.cards.update(cards => {
      const updated = [...cards];
      updated[index] = { ...updated[index], count, loading: false };
      return updated;
    });
  }
}
