import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../core/service/order-service';
import { OrderDetails } from '../../core/model/order-details';

@Component( {
  selector: 'app-orders',
  imports: [ CommonModule, RouterModule, CurrencyPipe, DatePipe ],
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
          <a
            [routerLink]="['/dashboard']"
            class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
            Start Shopping
          </a>
        </div>
      } @else {
        <div class="space-y-4">
          @for (order of orders(); track order.id) {
            <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">

              <!-- Order header -->
              <div
                class="flex items-center justify-between p-5 cursor-pointer hover:bg-gray-50 transition-colors"
                (click)="toggleExpand(order.id)">
                <div class="flex items-center gap-4">
                  <div>
                    <p class="font-semibold text-gray-900">Order #{{ order.id }}</p>
                    <p class="text-sm text-gray-500">
                      {{ order.createdAt | date: 'medium' }} · {{ order.itemCount }} item{{ order.itemCount !== 1 ? 's' : '' }}
                    </p>
                  </div>
                  <span [class]="statusClass(order.status)" class="px-2.5 py-0.5 rounded-full text-xs font-semibold">
                    {{ order.status }}
                  </span>
                </div>
                <div class="flex items-center gap-4">
                  <span class="font-bold text-gray-900 text-lg">{{ order.total | currency }}</span>
                  <svg
                    class="w-5 h-5 text-gray-400 transition-transform"
                    [class.rotate-180]="expandedOrders().has(order.id)"
                    fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                  </svg>
                </div>
              </div>

              <!-- Order items (expandable) -->
              @if (expandedOrders().has(order.id)) {
                <div class="border-t border-gray-100 px-5 pb-5 pt-4 bg-gray-50 space-y-3">
                  @for (item of order.items; track item.productId) {
                    <div class="flex items-center justify-between text-sm">
                      <div>
                        <span class="font-medium text-gray-900">{{ item.productName }}</span>
                        @if (item.brand) {
                          <span class="text-gray-500 ml-1">· {{ item.brand }}</span>
                        }
                      </div>
                      <div class="text-gray-600 flex gap-4">
                        <span>× {{ item.quantity }}</span>
                        <span class="font-medium text-gray-900 min-w-16 text-right">
                          {{ item.lineTotal | currency: (item.currency || 'USD') }}
                        </span>
                      </div>
                    </div>
                  }
                </div>
              }

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
  expandedOrders = signal( new Set<number>() );

  constructor( private orderService: OrderService ) {
  }

  ngOnInit(): void {
    this.orderService.getOrders().subscribe( {
      next: orders => {
        this.orders.set( orders );
        this.loading.set( false );
      },
      error: () => this.loading.set( false ),
    } );
  }

  toggleExpand( orderId: number ): void {
    const current = new Set( this.expandedOrders() );
    if ( current.has( orderId ) ) {
      current.delete( orderId );
    } else {
      current.add( orderId );
    }
    this.expandedOrders.set( current );
  }

  statusClass( status: string ): string {
    switch ( status ) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800';
      case 'SHIPPED':
        return 'bg-purple-100 text-purple-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
