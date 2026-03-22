import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { OrderService } from '../../core/service/order-service';
import { OrderSummary } from '../../core/model/order-summary';

@Component( {
  selector: 'app-order-summary',
  imports: [ CommonModule, RouterModule, CurrencyPipe, DatePipe ],
  template: `
    <div class="p-5 max-w-3xl mx-auto">

      <button (click)="goBack()"
              class="flex items-center gap-2 text-gray-500 hover:text-gray-800 mb-6 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Back to Orders
      </button>

      @if (loading()) {
        <div class="text-center py-20 text-gray-500">Loading order summary...</div>
      } @else if (error()) {
        <div class="text-center py-20">
          <p class="text-red-500 mb-4">{{ error() }}</p>
          <button (click)="goBack()"
                  class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-5 py-2 rounded-lg transition-colors">
            Go Back
          </button>
        </div>
      } @else if (summary()) {
        <!-- Order Header -->
        <div class="flex items-start justify-between mb-6">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">Order #{{ summary()!.id }}</h1>
            <p class="text-gray-500 mt-1">Placed on {{ summary()!.createdAt | date: 'longDate' }}</p>
          </div>
          <span [class]="statusClass(summary()!.status)"
                class="px-3 py-1 rounded-full text-sm font-semibold mt-1">
            {{ summary()!.status }}
          </span>
        </div>

        <!-- Items -->
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm mb-5">
          <div class="px-6 py-4 border-b border-gray-100">
            <h2 class="font-semibold text-gray-900">
              Items
              <span class="text-gray-400 font-normal ml-1">({{ summary()!.itemCount }})</span>
            </h2>
          </div>
          <div class="divide-y divide-gray-100">
            @for (item of summary()!.items; track item.productId) {
              <div class="px-6 py-4 flex items-start justify-between gap-4">
                <div class="flex-1 min-w-0">
                  <p class="font-medium text-gray-900">{{ item.productName }}</p>
                  @if (item.brand) {
                    <p class="text-sm text-gray-500">{{ item.brand }}</p>
                  }
                  @if (item.description) {
                    <p class="text-sm text-gray-400 mt-1 truncate">{{ item.description }}</p>
                  }
                </div>
                <div class="text-right shrink-0">
                  <p class="font-semibold text-gray-900">
                    {{ item.lineTotal | currency: (item.currency || 'USD') }}
                  </p>
                  <p class="text-sm text-gray-500">
                    {{ item.quantity }} × {{ item.price | currency: (item.currency || 'USD') }}
                  </p>
                </div>
              </div>
            }
          </div>
          <div class="px-6 py-4 border-t border-gray-100 flex justify-between items-center bg-gray-50 rounded-b-xl">
            <span class="font-semibold text-gray-900">Total</span>
            <span class="text-xl font-bold text-gray-900">{{ summary()!.total | currency }}</span>
          </div>
        </div>

        <!-- Payment Details -->
        @if (summary()!.payment) {
          <div class="bg-white border border-gray-200 rounded-xl shadow-sm">
            <div class="px-6 py-4 border-b border-gray-100">
              <h2 class="font-semibold text-gray-900">Payment</h2>
            </div>
            <div class="px-6 py-4 space-y-3">
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Status</span>
                <span [class]="paymentStatusClass(summary()!.payment.status)"
                      class="px-2.5 py-0.5 rounded-full text-xs font-semibold">
                  {{ summary()!.payment.status }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Amount charged</span>
                <span class="font-medium text-gray-900">
                  {{ summary()!.payment.amount | currency: summary()!.payment.currency }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Payment ID</span>
                <span class="text-gray-600 font-mono">#{{ summary()!.payment.id }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Date</span>
                <span class="text-gray-600">{{ summary()!.payment.createdAt | date: 'medium' }}</span>
              </div>
            </div>
          </div>
        }
      }
    </div>
  `,
  styles: ``
} )
export class OrderSummaryPage implements OnInit {

  summary = signal<OrderSummary | null>( null );
  loading = signal( true );
  error = signal<string | null>( null );

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
  ) {
  }

  ngOnInit(): void {
    const id = Number( this.route.snapshot.paramMap.get( 'id' ) );
    if ( !id ) {
      this.router.navigate( [ '/orders' ] );
      return;
    }
    this.orderService.getOrderSummary( id ).subscribe( {
      next: summary => {
        this.summary.set( summary );
        this.loading.set( false );
      },
      error: () => {
        this.error.set( 'Could not load order summary. Please try again.' );
        this.loading.set( false );
      },
    } );
  }

  goBack(): void {
    this.router.navigate( [ '/orders' ] );
  }

  statusClass( status: string ): string {
    switch ( status ) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED': return 'bg-blue-100 text-blue-800';
      case 'SHIPPED': return 'bg-purple-100 text-purple-800';
      case 'DELIVERED': return 'bg-green-100 text-green-800';
      case 'PAYMENT_FAILED': return 'bg-red-100 text-red-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  paymentStatusClass( status: string ): string {
    switch ( status ) {
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'FAILED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }
}
