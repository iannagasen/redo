import { Routes } from '@angular/router';
import { LoginCallback } from './login/callback';
import { Login } from './login/login';
import { Dashboard } from './dashboard/dashboard';
import { authGuard } from './login/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'login/callback', component: LoginCallback },
  { path: 'dashboard', component: Dashboard, canActivate: [ authGuard ] }
];
