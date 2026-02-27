import { Component, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CartService } from '../../core/service/cart-service';
import { OrderService } from '../../core/service/order-service';
import { CartItemDetails } from '../../core/model/cart-item-details';

@Component( {
  selector: 'app-cart',
  imports: [ CommonModule, RouterModule ],
  template: `
    <div class="p-5 max-w-4xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Your Cart</h1>

      @if (cartService.cartItems().length === 0) {
        <div class="text-center py-20">
          <svg class="mx-auto h-16 w-16 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
          </svg>
          <p class="text-xl text-gray-500 mb-2">Your cart is empty</p>
          <p class="text-gray-400 mb-6">Start adding products to see them here.</p>
          <a
            [routerLink]="['/dashboard']"
            class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
            Browse Products
          </a>
        </div>
      }

      @if (cartService.cartItems().length > 0) {
        <div class="flex flex-col lg:flex-row gap-8">

          <!-- Line items -->
          <div class="flex-1 space-y-4">
            @for (item of cartService.cartItems(); track item.productId) {
              <div class="bg-white border border-gray-200 rounded-xl p-5 flex items-start gap-4 shadow-sm">

                <!-- Product info -->
                <div class="flex-1 min-w-0">
                  <a
                    [routerLink]="['/products', item.productId]"
                    class="text-lg font-semibold text-gray-900 hover:text-blue-600 transition-colors block truncate">
                    {{ item.productName }}
                  </a>
                  @if (item.brand) {
                    <p class="text-sm text-gray-500 mt-0.5">{{ item.brand }}</p>
                  }
                  @if (item.price) {
                    <p class="text-sm text-gray-500 mt-1">
                      {{ item.price | currency: (item.currency || 'USD') }} each
                    </p>
                  }
                </div>

                <!-- Quantity controls -->
                <div class="flex items-center gap-2">
                  <button
                    (click)="decrement(item)"
                    class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-600
                           hover:border-blue-500 hover:text-blue-600 transition-colors font-bold text-lg leading-none">
                    −
                  </button>
                  <span class="w-8 text-center font-semibold text-gray-900">{{ item.quantity }}</span>
                  <button
                    (click)="cartService.updateQuantity(item.productId, item.quantity + 1)"
                    class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-600
                           hover:border-blue-500 hover:text-blue-600 transition-colors font-bold text-lg leading-none">
                    +
                  </button>
                </div>

                <!-- Line total -->
                @if (item.lineTotal) {
                  <div class="text-right min-w-20">
                    <p class="font-bold text-gray-900">
                      {{ item.lineTotal | currency: (item.currency || 'USD') }}
                    </p>
                  </div>
                }

                <!-- Remove -->
                <button
                  (click)="cartService.removeFromCart(item.productId)"
                  class="text-gray-400 hover:text-red-500 transition-colors ml-1 shrink-0">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                  </svg>
                </button>

              </div>
            }

            <!-- Clear cart -->
            <div class="text-right">
              <button
                (click)="cartService.clearCart()"
                class="text-sm text-red-500 hover:text-red-700 underline transition-colors">
                Clear cart
              </button>
            </div>
          </div>

          <!-- Order summary -->
          <div class="lg:w-80">
            <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm sticky top-24">
              <h2 class="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>

              <div class="space-y-2 text-sm text-gray-600 mb-4">
                <div class="flex justify-between">
                  <span>Items ({{ cartService.itemCount() }})</span>
                  <span>{{ cartService.total() | currency }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Shipping</span>
                  <span class="text-green-600 font-medium">Free</span>
                </div>
              </div>

              <div class="border-t border-gray-200 pt-4 mb-6">
                <div class="flex justify-between font-bold text-gray-900">
                  <span>Total</span>
                  <span class="text-xl">{{ cartService.total() | currency }}</span>
                </div>
              </div>

              @if (checkoutError()) {
                <div class="bg-red-50 border border-red-200 rounded-lg p-3 mb-3 text-sm text-red-700">
                  Checkout failed. Please try again.
                </div>
              }
              <button
                (click)="checkout()"
                class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-semibold transition-colors mb-3">
                Checkout
              </button>
              <a
                [routerLink]="['/dashboard']"
                class="block text-center text-sm text-blue-600 hover:text-blue-800 transition-colors">
                Continue Shopping
              </a>
            </div>
          </div>

        </div>
      }

    </div>
  `,
  styles: ``
} )
export class Cart {

  orderPlaced = signal( false );
  checkoutError = signal( false );

  constructor(
    public cartService: CartService,
    private orderService: OrderService,
    private router: Router,
  ) {
  }

  decrement( item: CartItemDetails ): void {
    this.cartService.updateQuantity( item.productId, item.quantity - 1 );
  }

  checkout(): void {
    const items = this.cartService.cartItems();
    this.orderService.createOrder( items ).subscribe( {
      next: order => {
        this.cartService.clearCart();
        this.router.navigate( [ '/checkout' ], { queryParams: { orderId: order.id } } );
      },
      error: () => this.checkoutError.set( true ),
    } );
  }
}
