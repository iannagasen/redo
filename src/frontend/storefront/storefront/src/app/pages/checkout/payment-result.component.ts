import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterModule } from '@angular/router';

/**
 * Displays the final payment outcome (success or failure).
 *
 * Usage:
 *   <app-payment-result [status]="order()!.status" [orderId]="order()!.id" (retry)="onRetry()" />
 */
@Component( {
  selector: 'app-payment-result',
  standalone: true,
  imports: [ RouterModule ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (status() === 'CONFIRMED') {
      <div class="bg-green-50 border border-green-200 rounded-xl p-8 text-center" role="status">
        <svg class="mx-auto h-16 w-16 text-green-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"
             aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <h2 class="text-2xl font-bold text-green-800 mb-2">Payment Successful!</h2>
        <p class="text-green-700 mb-6">Order #{{ orderId() }} has been confirmed.</p>
        <a [routerLink]="['/orders']"
           class="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
          View Orders
        </a>
      </div>
    } @else if (status() === 'PAYMENT_FAILED') {
      <div class="bg-red-50 border border-red-200 rounded-xl p-8 text-center" role="alert">
        <svg class="mx-auto h-16 w-16 text-red-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"
             aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <h2 class="text-2xl font-bold text-red-800 mb-2">Payment Failed</h2>
        <p class="text-red-700 mb-6">Your card was declined. Please try a different card.</p>
        <button (click)="retry.emit()"
                class="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
          Try Again
        </button>
      </div>
    }
  `,
} )
export class PaymentResultComponent {
  status = input.required<string>();
  orderId = input.required<number>();
  retry = output<void>();
}
