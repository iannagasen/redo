import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Pod, PodContainer } from '../model/pod';
import { Deployment } from '../model/deployment';
import { K8sService } from '../model/k8s-service';
import { ConfigMap } from '../model/config-map';

@Injectable({ providedIn: 'root' })
export class K8sApiService {
  private http = inject(HttpClient);
  private BASE_URL = 'http://shopbuddy.com/k8s-admin';

  getPods(): Observable<Pod[]> {
    return this.http.get<any>(`${this.BASE_URL}/pods`).pipe(
      map(res => (res.items ?? []).map((item: any) => this.mapPod(item)))
    );
  }

  getPod(name: string): Observable<Pod> {
    return this.http.get<any>(`${this.BASE_URL}/pods/${name}`).pipe(
      map(item => this.mapPod(item))
    );
  }

  getPodLogs(name: string): Observable<string> {
    return this.http.get(`${this.BASE_URL}/pods/${name}/logs`, { responseType: 'text' });
  }

  getDeployments(): Observable<Deployment[]> {
    return this.http.get<any>(`${this.BASE_URL}/deployments`).pipe(
      map(res => (res.items ?? []).map((item: any) => this.mapDeployment(item)))
    );
  }

  getServices(): Observable<K8sService[]> {
    return this.http.get<any>(`${this.BASE_URL}/services`).pipe(
      map(res => (res.items ?? []).map((item: any) => this.mapService(item)))
    );
  }

  getConfigMaps(): Observable<ConfigMap[]> {
    return this.http.get<any>(`${this.BASE_URL}/configmaps`).pipe(
      map(res => (res.items ?? []).map((item: any) => this.mapConfigMap(item)))
    );
  }

  private mapPod(item: any): Pod {
    const meta = item.metadata ?? {};
    const spec = item.spec ?? {};
    const status = item.status ?? {};
    const containerStatuses: any[] = status.containerStatuses ?? [];

    const totalRestarts = containerStatuses.reduce(
      (sum: number, cs: any) => sum + (cs.restartCount ?? 0), 0
    );
    const readyCount = containerStatuses.filter((cs: any) => cs.ready).length;
    const totalCount = containerStatuses.length;

    const containers: PodContainer[] = containerStatuses.map((cs: any) => ({
      name: cs.name ?? '',
      image: cs.image ?? '',
      ready: cs.ready ?? false,
      restartCount: cs.restartCount ?? 0,
      state: Object.keys(cs.state ?? {})[0] ?? 'unknown',
    }));

    return {
      name: meta.name ?? '',
      namespace: meta.namespace ?? '',
      status: status.phase ?? 'Unknown',
      ready: `${readyCount}/${totalCount}`,
      restarts: totalRestarts,
      age: this.computeAge(meta.creationTimestamp),
      nodeName: spec.nodeName ?? '',
      podIP: status.podIP ?? '',
      containers,
    };
  }

  private mapDeployment(item: any): Deployment {
    const meta = item.metadata ?? {};
    const status = item.status ?? {};
    const spec = item.spec ?? {};

    return {
      name: meta.name ?? '',
      namespace: meta.namespace ?? '',
      replicas: spec.replicas ?? 0,
      readyReplicas: status.readyReplicas ?? 0,
      updatedReplicas: status.updatedReplicas ?? 0,
      availableReplicas: status.availableReplicas ?? 0,
      age: this.computeAge(meta.creationTimestamp),
    };
  }

  private mapService(item: any): K8sService {
    const meta = item.metadata ?? {};
    const spec = item.spec ?? {};
    const ports = (spec.ports ?? [])
      .map((p: any) => `${p.port}${p.targetPort ? ':' + p.targetPort : ''}/${p.protocol ?? 'TCP'}`)
      .join(', ');

    return {
      name: meta.name ?? '',
      namespace: meta.namespace ?? '',
      type: spec.type ?? '',
      clusterIP: spec.clusterIP ?? '',
      ports,
      age: this.computeAge(meta.creationTimestamp),
    };
  }

  private mapConfigMap(item: any): ConfigMap {
    const meta = item.metadata ?? {};
    return {
      name: meta.name ?? '',
      namespace: meta.namespace ?? '',
      dataKeys: Object.keys(item.data ?? {}),
      age: this.computeAge(meta.creationTimestamp),
    };
  }

  private computeAge(timestamp: string | undefined): string {
    if (!timestamp) return 'Unknown';
    const created = new Date(timestamp).getTime();
    const now = Date.now();
    const diffMs = now - created;
    const diffSec = Math.floor(diffMs / 1000);

    if (diffSec < 60) return `${diffSec}s`;
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin}m`;
    const diffHours = Math.floor(diffMin / 60);
    if (diffHours < 24) return `${diffHours}h`;
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays}d`;
  }
}
