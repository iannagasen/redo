import { Routes } from '@angular/router';
import { Overview } from './pages/overview/overview';
import { Pods } from './pages/pods/pods';
import { PodDetail } from './pages/pods/pod-detail';
import { Deployments } from './pages/deployments/deployments';
import { Services } from './pages/services/services';
import { ConfigMaps } from './pages/config-maps/config-maps';
import { Login } from './pages/login/login';
import { LoginCallback } from './pages/login/callback';
import { authGuard } from './core/guard/auth-guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'login/callback', component: LoginCallback },
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'overview', component: Overview, canActivate: [authGuard] },
  { path: 'pods', component: Pods, canActivate: [authGuard] },
  { path: 'pods/:name', component: PodDetail, canActivate: [authGuard] },
  { path: 'deployments', component: Deployments, canActivate: [authGuard] },
  { path: 'services', component: Services, canActivate: [authGuard] },
  { path: 'configmaps', component: ConfigMaps, canActivate: [authGuard] },
];
