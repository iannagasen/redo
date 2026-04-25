import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartItemDetails } from '../../core/model/cart-item-details';

/**
 * Presentational row for a single cart item.
 * Emits decrement / increment / remove — caller owns the mutation logic.
 */
@Component( {
  selector: 'app-cart-item-row',
  standalone: true,
  imports: [ CurrencyPipe, RouterModule ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="bg-white border border-gray-200 rounded-xl p-5 flex items-start gap-4 shadow-sm">

      <!-- Product info -->
      <div class="flex-1 min-w-0">
        <a [routerLink]="['/products', item().productId]"
           class="text-lg font-semibold text-gray-900 hover:text-blue-600 transition-colors block truncate">
          {{ item().productName }}
        </a>
        @if (item().brand) {
          <p class="text-sm text-gray-500 mt-0.5">{{ item().brand }}</p>
        }
        @if (item().price) {
          <p class="text-sm text-gray-500 mt-1">
            {{ item().price | currency: (item().currency || 'USD') }} each
          </p>
        }
      </div>

      <!-- Quantity controls -->
      <div class="flex items-center gap-2">
        <button (click)="decrement.emit()"
                class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center
                       text-gray-600 hover:border-blue-500 hover:text-blue-600 transition-colors
                       font-bold text-lg leading-none"
                aria-label="Decrease quantity">
          −
        </button>
        <span class="w-8 text-center font-semibold text-gray-900">{{ item().quantity }}</span>
        <button (click)="increment.emit()"
                class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center
                       text-gray-600 hover:border-blue-500 hover:text-blue-600 transition-colors
                       font-bold text-lg leading-none"
                aria-label="Increase quantity">
          +
        </button>
      </div>

      <!-- Line total -->
      @if (item().lineTotal) {
        <div class="text-right min-w-20">
          <p class="font-bold text-gray-900">
            {{ item().lineTotal | currency: (item().currency || 'USD') }}
          </p>
        </div>
      }

      <!-- Remove -->
      <button (click)="remove.emit()"
              class="text-gray-400 hover:text-red-500 transition-colors ml-1 shrink-0"
              aria-label="Remove item">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
        </svg>
      </button>
    </div>
  `,
} )
export class CartItemRowComponent {
  item = input.required<CartItemDetails>();
  decrement = output<void>();
  increment = output<void>();
  remove = output<void>();
}
