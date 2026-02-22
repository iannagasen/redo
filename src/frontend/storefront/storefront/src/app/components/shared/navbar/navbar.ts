import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OauthService } from '../../../core/service/oauth-service';
import { CartService } from '../../../core/service/cart-service';

@Component( {
  selector: 'app-navbar',
  imports: [ CommonModule, RouterModule ],
  template: `
    @if (auth.isLoggedIn()) {
      <nav class="bg-white border-b border-gray-200 sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-5 py-3 flex items-center justify-between">

          <a [routerLink]="['/dashboard']" class="text-xl font-bold text-blue-600 hover:text-blue-700 transition-colors">
            ShopBuddy
          </a>

          <div class="flex items-center gap-2">
            <a
              [routerLink]="['/orders']"
              class="flex items-center gap-1.5 text-gray-600 hover:text-blue-600 transition-colors p-2 rounded-lg hover:bg-blue-50">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
              </svg>
              <span class="text-sm font-medium hidden sm:inline">Orders</span>
            </a>

            <a
              [routerLink]="['/cart']"
              class="relative flex items-center gap-2 text-gray-600 hover:text-blue-600 transition-colors p-2 rounded-lg hover:bg-blue-50">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
              </svg>
              @if (cart.itemCount() > 0) {
                <span
                  class="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                  {{ cart.itemCount() > 99 ? '99+' : cart.itemCount() }}
                </span>
              }
              <span class="text-sm font-medium hidden sm:inline">Cart</span>
            </a>
          </div>

        </div>
      </nav>
    }
  `,
  styles: ``
} )
export class NavBar implements OnInit {
  constructor(
    public auth: OauthService,
    public cart: CartService,
  ) {
  }

  ngOnInit(): void {
    if ( this.auth.isLoggedIn() ) {
      this.cart.loadCart().subscribe();
    }
  }
}
