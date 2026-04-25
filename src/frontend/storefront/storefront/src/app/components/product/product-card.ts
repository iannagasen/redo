import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ProductDetails } from '../../core/model/product-details';

/**
 * Presentational card for a single product.
 * Usage: <app-product-card [product]="p" /> — wrap in a <a [routerLink]> for navigation.
 */
@Component( {
  selector: 'app-product-card',
  standalone: true,
  imports: [ CurrencyPipe ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm hover:shadow-md
                hover:-translate-y-0.5 transition-all duration-200 cursor-pointer h-full">
      <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ product().name }}</h3>

      @if (product().description) {
        <p class="text-gray-600 mb-3 text-sm leading-relaxed line-clamp-2">{{ product().description }}</p>
      }

      @if (product().brand) {
        <p class="text-xs text-gray-400 mb-2">{{ product().brand }}</p>
      }

      @if (product().price) {
        <p class="text-lg font-bold text-green-600">
          {{ product().price | currency: (product().currency || 'USD') }}
        </p>
      }

      <p class="text-xs text-blue-500 mt-3 font-medium">View details →</p>
    </div>
  `,
} )
export class ProductCard {
  product = input.required<ProductDetails>();
}
