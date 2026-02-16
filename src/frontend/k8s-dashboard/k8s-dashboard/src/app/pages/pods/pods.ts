import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { K8sApiService } from '../../core/service/k8s-api-service';
import { Pod } from '../../core/model/pod';

@Component({
  selector: 'app-pods',
  template: `
    <h1 class="text-2xl font-bold text-gray-800 mb-6">Pods</h1>

    @if (loading()) {
      <p class="text-gray-500">Loading pods...</p>
    } @else {
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ready</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Restarts</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Age</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            @for (pod of pods(); track pod.name) {
              <tr
                (click)="viewPod(pod.name)"
                class="hover:bg-gray-50 cursor-pointer transition-colors"
              >
                <td class="px-4 py-3 text-sm font-medium text-blue-600">{{ pod.name }}</td>
                <td class="px-4 py-3 text-sm">
                  <span
                    class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold"
                    [class]="pod.status === 'Running' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'"
                  >
                    {{ pod.status }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ pod.ready }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ pod.restarts }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ pod.age }}</td>
                <td class="px-4 py-3 text-sm text-gray-500">{{ pod.podIP }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class Pods implements OnInit {
  private api = inject(K8sApiService);
  private router = inject(Router);

  pods = signal<Pod[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.api.getPods().subscribe(pods => {
      this.pods.set(pods);
      this.loading.set(false);
    });
  }

  viewPod(name: string) {
    this.router.navigate(['/pods', name]);
  }
}
