import { Routes } from '@angular/router';
import { LoginCallback } from './pages/login/callback';
import { Login } from './pages/login/login';
import { Dashboard } from './pages/dashboard/dashboard';
import { ProductDetail } from './pages/product-detail/product-detail';
import { Cart } from './pages/cart/cart';
import { Orders } from './pages/orders/orders';
import { Checkout } from './pages/checkout/checkout';
import { authGuard } from './pages/login/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'login/callback', component: LoginCallback },
  { path: 'dashboard', component: Dashboard, canActivate: [ authGuard ] },
  { path: 'products/:id', component: ProductDetail, canActivate: [ authGuard ] },
  { path: 'cart', component: Cart, canActivate: [ authGuard ] },
  { path: 'checkout', component: Checkout, canActivate: [ authGuard ] },
  { path: 'orders', component: Orders, canActivate: [ authGuard ] }
];
