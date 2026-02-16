import { Component, inject, signal, OnInit } from '@angular/core';
import { K8sApiService } from '../../core/service/k8s-api-service';
import { ConfigMap } from '../../core/model/config-map';

@Component({
  selector: 'app-config-maps',
  template: `
    <h1 class="text-2xl font-bold text-gray-800 mb-6">ConfigMaps</h1>

    @if (loading()) {
      <p class="text-gray-500">Loading configmaps...</p>
    } @else {
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Namespace</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Data Keys</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Age</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            @for (cm of configMaps(); track cm.name) {
              <tr class="hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ cm.name }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ cm.namespace }}</td>
                <td class="px-4 py-3 text-sm text-gray-500">
                  @for (key of cm.dataKeys; track key) {
                    <span class="inline-flex px-1.5 py-0.5 rounded bg-gray-100 text-gray-700 text-xs mr-1 mb-1">
                      {{ key }}
                    </span>
                  }
                </td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ cm.age }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class ConfigMaps implements OnInit {
  private api = inject(K8sApiService);

  configMaps = signal<ConfigMap[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.api.getConfigMaps().subscribe(cms => {
      this.configMaps.set(cms);
      this.loading.set(false);
    });
  }
}
