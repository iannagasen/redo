import { Component, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { CartService } from '../../core/service/cart-service';
import { OrderService } from '../../core/service/order-service';
import { CartItemRowComponent } from './cart-item-row.component';

@Component( {
  selector: 'app-cart',
  imports: [ RouterModule, CurrencyPipe, CartItemRowComponent ],
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
          <a [routerLink]="['/dashboard']"
             class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
            Browse Products
          </a>
        </div>
      } @else {
        <div class="flex flex-col lg:flex-row gap-8">

          <!-- Line items -->
          <div class="flex-1 space-y-4">
            @for (item of cartService.cartItems(); track item.productId) {
              <app-cart-item-row
                [item]="item"
                (decrement)="cartService.updateQuantity(item.productId, item.quantity - 1)"
                (increment)="cartService.updateQuantity(item.productId, item.quantity + 1)"
                (remove)="cartService.removeFromCart(item.productId)" />
            }
            <div class="text-right">
              <button (click)="cartService.clearCart()"
                      class="text-sm text-red-500 hover:text-red-700 underline transition-colors">
                Clear cart
              </button>
            </div>
          </div>

          <!-- Order summary sidebar -->
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
                <div class="bg-red-50 border border-red-200 rounded-lg p-3 mb-3 text-sm text-red-700"
                     role="alert">
                  Checkout failed. Please try again.
                </div>
              }

              <button (click)="checkout()"
                      class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-semibold transition-colors mb-3">
                Checkout
              </button>
              <a [routerLink]="['/dashboard']"
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

  checkoutError = signal( false );

  constructor(
    public cartService: CartService,
    private orderService: OrderService,
    private router: Router,
  ) {}

  checkout(): void {
    this.checkoutError.set( false );
    this.orderService.createOrder( this.cartService.cartItems() ).subscribe( {
      next: order => {
        this.cartService.clearCart();
        this.router.navigate( [ '/checkout' ], { queryParams: { orderId: order.id } } );
      },
      error: () => this.checkoutError.set( true ),
    } );
  }
}
