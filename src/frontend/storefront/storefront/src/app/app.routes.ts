import { Routes } from '@angular/router';
import { LoginCallback } from './pages/login/callback';
import { Login } from './pages/login/login';
import { Dashboard } from './pages/dashboard/dashboard';
import { authGuard } from './pages/login/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'login/callback', component: LoginCallback },
  { path: 'dashboard', component: Dashboard, canActivate: [ authGuard ] }
];
