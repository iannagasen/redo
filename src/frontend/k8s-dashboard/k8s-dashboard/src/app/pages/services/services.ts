import { Component, inject, signal, OnInit } from '@angular/core';
import { K8sApiService } from '../../core/service/k8s-api-service';
import { K8sService } from '../../core/model/k8s-service';

@Component({
  selector: 'app-services',
  template: `
    <h1 class="text-2xl font-bold text-gray-800 mb-6">Services</h1>

    @if (loading()) {
      <p class="text-gray-500">Loading services...</p>
    } @else {
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Cluster IP</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ports</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Age</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            @for (svc of services(); track svc.name) {
              <tr class="hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ svc.name }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">
                  <span class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">
                    {{ svc.type }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-500 font-mono">{{ svc.clusterIP }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ svc.ports }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ svc.age }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class Services implements OnInit {
  private api = inject(K8sApiService);

  services = signal<K8sService[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.api.getServices().subscribe(svcs => {
      this.services.set(svcs);
      this.loading.set(false);
    });
  }
}
