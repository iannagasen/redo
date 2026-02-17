import { Component, inject, signal, OnInit } from '@angular/core';
import { K8sApiService } from '../../core/service/k8s-api-service';
import { Deployment } from '../../core/model/deployment';

@Component({
  selector: 'app-deployments',
  template: `
    <h1 class="text-2xl font-bold text-gray-800 mb-6">Deployments</h1>

    @if (loading()) {
      <p class="text-gray-500">Loading deployments...</p>
    } @else {
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ready</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Up-to-date</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Available</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Age</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            @for (dep of deployments(); track dep.name) {
              <tr class="hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ dep.name }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ dep.readyReplicas }}/{{ dep.replicas }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ dep.updatedReplicas }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ dep.availableReplicas }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ dep.age }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class Deployments implements OnInit {
  private api = inject(K8sApiService);

  deployments = signal<Deployment[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.api.getDeployments().subscribe(deps => {
      this.deployments.set(deps);
      this.loading.set(false);
    });
  }
}
