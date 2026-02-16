import { Routes } from '@angular/router';
import { Overview } from './pages/overview/overview';
import { Pods } from './pages/pods/pods';
import { PodDetail } from './pages/pods/pod-detail';
import { Deployments } from './pages/deployments/deployments';
import { Services } from './pages/services/services';
import { ConfigMaps } from './pages/config-maps/config-maps';

export const routes: Routes = [
  { path: '', component: Overview },
  { path: 'pods', component: Pods },
  { path: 'pods/:name', component: PodDetail },
  { path: 'deployments', component: Deployments },
  { path: 'services', component: Services },
  { path: 'configmaps', component: ConfigMaps },
];
