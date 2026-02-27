import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { PaymentService } from '../../core/service/payment.service';
import { OrderService } from '../../core/service/order-service';
import { OrderDetails } from '../../core/model/order-details';

@Component( {
  selector: 'app-checkout',
  imports: [ CommonModule, RouterModule, CurrencyPipe ],
  template: `
    <div class="p-5 max-w-2xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Checkout</h1>

      @if (loading()) {
        <div class="text-center py-20 text-gray-500">Loading order details...</div>
      } @else if (!order()) {
        <div class="text-center py-20 text-gray-500">Order not found.</div>
      } @else if (settled()) {
        <!-- Final state after payment -->
        @if (order()!.status === 'CONFIRMED') {
          <div class="bg-green-50 border border-green-200 rounded-xl p-8 text-center">
            <svg class="mx-auto h-16 w-16 text-green-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <h2 class="text-2xl font-bold text-green-800 mb-2">Payment Successful!</h2>
            <p class="text-green-700 mb-6">Order #{{ order()!.id }} has been confirmed.</p>
            <a [routerLink]="['/orders']"
               class="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
              View Orders
            </a>
          </div>
        } @else if (order()!.status === 'PAYMENT_FAILED') {
          <div class="bg-red-50 border border-red-200 rounded-xl p-8 text-center">
            <svg class="mx-auto h-16 w-16 text-red-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <h2 class="text-2xl font-bold text-red-800 mb-2">Payment Failed</h2>
            <p class="text-red-700 mb-6">Your card was declined. Please try a different card.</p>
            <button (click)="resetForm()"
                    class="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-lg font-semibold transition-colors">
              Try Again
            </button>
          </div>
        }
      } @else {
        <!-- Payment form -->
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-1">Order Summary</h2>
          <p class="text-sm text-gray-500 mb-4">Order #{{ order()!.id }} · {{ order()!.itemCount }} item(s)</p>
          <div class="flex justify-between font-bold text-gray-900 text-lg border-t border-gray-100 pt-4">
            <span>Total</span>
            <span>{{ order()!.total | currency }}</span>
          </div>
        </div>

        <div class="bg-white border border-gray-200 rounded-xl shadow-sm p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Payment Details</h2>

          <div class="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-5 text-sm text-blue-700">
            <strong>Test mode:</strong> Use any card number to succeed. Use
            <code class="font-mono bg-blue-100 px-1 rounded">4000000000000002</code> to simulate a decline.
          </div>

          @if (paymentError()) {
            <div class="bg-red-50 border border-red-200 rounded-lg p-3 mb-4 text-sm text-red-700">
              {{ paymentError() }}
            </div>
          }

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Cardholder Name</label>
            <input
              type="text"
              (input)="cardholderName.set($any($event.target).value)"
              placeholder="Jane Doe"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic"/>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Card Number</label>
            <input
              type="text"
              (input)="cardNumber.set($any($event.target).value)"
              maxlength="19"
              placeholder="4242 4242 4242 4242"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 font-mono focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic placeholder:font-sans"/>
          </div>

          <div class="flex gap-4 mb-6">
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700 mb-1">Expiry Month</label>
              <input
                type="number"
                (input)="expiryMonth.set(+$any($event.target).value)"
                min="1" max="12"
                placeholder="MM"
                class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic"/>
            </div>
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700 mb-1">Expiry Year</label>
              <input
                type="number"
                (input)="expiryYear.set(+$any($event.target).value)"
                min="2025"
                placeholder="YYYY"
                class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic"/>
            </div>
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700 mb-1">CVV</label>
              <input
                type="text"
                (input)="cvv.set($any($event.target).value)"
                maxlength="4"
                placeholder="123"
                class="w-full border border-gray-300 rounded-lg px-3 py-2 font-mono focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic placeholder:font-sans"/>
            </div>
          </div>

          <button
            type="button"
            (click)="submitPayment()"
            [disabled]="submitting()"
            class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white py-3 rounded-lg font-semibold transition-colors">
            @if (submitting()) {
              Processing...
            } @else {
              Pay {{ order()!.total | currency }}
            }
          </button>
        </div>
      }
    </div>
  `,
  styles: ``
} )
export class Checkout implements OnInit {

  order = signal<OrderDetails | null>( null );
  loading = signal( true );
  submitting = signal( false );
  settled = signal( false );
  paymentError = signal<string | null>( null );

  cardholderName = signal( '' );
  cardNumber = signal( '' );
  expiryMonth = signal( 0 );
  expiryYear = signal( 0 );
  cvv = signal( '' );

  private orderId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private orderService: OrderService,
  ) {
  }

  ngOnInit(): void {
    this.orderId = Number( this.route.snapshot.queryParamMap.get( 'orderId' ) );
    if ( !this.orderId ) {
      this.router.navigate( [ '/orders' ] );
      return;
    }
    this.orderService.getOrderById( this.orderId ).subscribe( {
      next: order => {
        this.order.set( order );
        this.loading.set( false );
      },
      error: () => {
        this.loading.set( false );
      },
    } );
  }

  submitPayment(): void {
    const ord = this.order();
    if ( !ord ) return;

    if ( !this.cardholderName() || !this.cardNumber() || !this.expiryMonth() || !this.expiryYear() || !this.cvv() ) {
      this.paymentError.set( 'Please fill in all card details.' );
      return;
    }

    this.submitting.set( true );
    this.paymentError.set( null );

    this.paymentService.initiatePayment( {
      orderId: ord.id,
      amount: ord.total,
      currency: 'USD',
      cardNumber: this.cardNumber().replaceAll( ' ', '' ),
      cardholderName: this.cardholderName(),
      expiryMonth: this.expiryMonth(),
      expiryYear: this.expiryYear(),
      cvv: this.cvv(),
    } ).subscribe( {
      next: () => {
        this.submitting.set( false );
        this.pollForResult();
      },
      error: () => {
        this.submitting.set( false );
        this.paymentError.set( 'Payment request failed. Please try again.' );
      },
    } );
  }

  private pollForResult(): void {
    this.paymentService.pollOrderUntilSettled( this.orderId ).subscribe( {
      next: order => {
        this.order.set( order );
        if ( order.status !== 'PENDING' ) {
          this.settled.set( true );
        }
      },
    } );
  }

  resetForm(): void {
    this.settled.set( false );
    this.paymentError.set( null );
    this.cardholderName.set( '' );
    this.cardNumber.set( '' );
    this.expiryMonth.set( 0 );
    this.expiryYear.set( 0 );
    this.cvv.set( '' );
  }
}
