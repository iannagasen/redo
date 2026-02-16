import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { K8sApiService } from '../../core/service/k8s-api-service';
import { Pod } from '../../core/model/pod';

@Component({
  selector: 'app-pod-detail',
  template: `
    <div class="mb-4">
      <button (click)="goBack()" class="text-blue-600 hover:text-blue-800 text-sm cursor-pointer">
        &larr; Back to Pods
      </button>
    </div>

    @if (loading()) {
      <p class="text-gray-500">Loading pod details...</p>
    } @else if (pod(); as p) {
      <h1 class="text-2xl font-bold text-gray-800 mb-6">{{ p.name }}</h1>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-gray-700 mb-4">Pod Info</h2>
          <dl class="space-y-2 text-sm">
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Namespace</dt>
              <dd class="text-gray-900">{{ p.namespace }}</dd>
            </div>
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Status</dt>
              <dd>
                <span
                  class="inline-flex px-2 py-0.5 rounded-full text-xs font-semibold"
                  [class]="p.status === 'Running' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'"
                >
                  {{ p.status }}
                </span>
              </dd>
            </div>
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Ready</dt>
              <dd class="text-gray-900">{{ p.ready }}</dd>
            </div>
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Restarts</dt>
              <dd class="text-gray-900">{{ p.restarts }}</dd>
            </div>
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Age</dt>
              <dd class="text-gray-900">{{ p.age }}</dd>
            </div>
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Node</dt>
              <dd class="text-gray-900">{{ p.nodeName }}</dd>
            </div>
            <div class="flex">
              <dt class="w-32 font-medium text-gray-500">Pod IP</dt>
              <dd class="text-gray-900">{{ p.podIP }}</dd>
            </div>
          </dl>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-gray-700 mb-4">Containers</h2>
          @for (c of p.containers; track c.name) {
            <div class="border rounded p-3 mb-2 last:mb-0">
              <div class="font-medium text-gray-800">{{ c.name }}</div>
              <div class="text-xs text-gray-500 mt-1">{{ c.image }}</div>
              <div class="flex gap-3 mt-2 text-xs">
                <span
                  class="px-2 py-0.5 rounded-full font-semibold"
                  [class]="c.ready ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                >
                  {{ c.ready ? 'Ready' : 'Not Ready' }}
                </span>
                <span class="text-gray-500">State: {{ c.state }}</span>
                <span class="text-gray-500">Restarts: {{ c.restartCount }}</span>
              </div>
            </div>
          }
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-700">Logs</h2>
          <button
            (click)="fetchLogs()"
            class="text-sm px-3 py-1.5 bg-blue-600 text-white rounded hover:bg-blue-700 cursor-pointer"
          >
            {{ logsLoading() ? 'Loading...' : 'Fetch Logs' }}
          </button>
        </div>
        @if (logs()) {
          <pre class="bg-gray-900 text-green-400 text-xs p-4 rounded overflow-auto max-h-96 whitespace-pre-wrap">{{ logs() }}</pre>
        } @else {
          <p class="text-gray-400 text-sm">Click "Fetch Logs" to view pod logs.</p>
        }
      </div>
    }
  `,
})
export class PodDetail implements OnInit {
  private api = inject(K8sApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  pod = signal<Pod | null>(null);
  loading = signal(true);
  logs = signal<string | null>(null);
  logsLoading = signal(false);

  private podName = '';

  ngOnInit() {
    this.podName = this.route.snapshot.params['name'];
    this.api.getPod(this.podName).subscribe(pod => {
      this.pod.set(pod);
      this.loading.set(false);
    });
  }

  fetchLogs() {
    this.logsLoading.set(true);
    this.api.getPodLogs(this.podName).subscribe({
      next: logs => {
        this.logs.set(logs);
        this.logsLoading.set(false);
      },
      error: () => {
        this.logs.set('Failed to fetch logs.');
        this.logsLoading.set(false);
      },
    });
  }

  goBack() {
    this.router.navigate(['/pods']);
  }
}
