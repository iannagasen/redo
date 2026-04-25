import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { OrderService } from '../../core/service/order-service';
import { OrderDetails } from '../../core/model/order-details';
import { OrderStatusPipe } from '../../core/pipes/order-status.pipe';

@Component( {
  selector: 'app-orders',
  imports: [ CommonModule, RouterModule, CurrencyPipe, DatePipe, OrderStatusPipe ],
  template: `
    <div class="p-5 max-w-4xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Your Orders</h1>

      @if (loading()) {
        <div class="text-center py-20 text-gray-500">Loading orders...</div>
      } @else if (orders().length === 0) {
        <div class="text-center py-20">
          <svg class="mx-auto h-16 w-16 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
          </svg>
          <p class="text-xl text-gray-500 mb-2">No orders yet</p>
          <p class="text-gray-400 mb-6">Your order history will appear here.</p>
          <a [routerLink]="['/dashboard']"
             class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
            Start Shopping
          </a>
        </div>
      } @else {
        <div class="space-y-3">
          @for (order of orders(); track order.id) {
            <div class="bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md
                        hover:border-gray-300 transition-all cursor-pointer"
                 (click)="viewSummary(order.id)">
              <div class="flex items-center justify-between p-5">
                <div class="flex items-center gap-4">
                  <div>
                    <p class="font-semibold text-gray-900">Order #{{ order.id }}</p>
                    <p class="text-sm text-gray-500">
                      {{ order.createdAt | date: 'mediumDate' }} ·
                      {{ order.itemCount }} item{{ order.itemCount !== 1 ? 's' : '' }}
                    </p>
                  </div>
                  <span [class]="order.status | orderStatus"
                        class="px-2.5 py-0.5 rounded-full text-xs font-semibold">
                    {{ order.status }}
                  </span>
                </div>
                <div class="flex items-center gap-3">
                  <span class="font-bold text-gray-900 text-lg">{{ order.total | currency }}</span>
                  <svg class="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                  </svg>
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: ``
} )
export class Orders implements OnInit {

  orders = signal<OrderDetails[]>( [] );
  loading = signal( true );

  constructor(
    private orderService: OrderService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe( {
      next: orders => { this.orders.set( orders ); this.loading.set( false ); },
      error: () => this.loading.set( false ),
    } );
  }

  viewSummary( orderId: number ): void {
    this.router.navigate( [ '/orders', orderId ] );
  }
}
