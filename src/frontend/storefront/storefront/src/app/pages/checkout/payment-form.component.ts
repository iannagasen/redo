import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { OrderDetails } from '../../core/model/order-details';

export interface CardDetails {
  cardholderName: string;
  cardNumber: string;
  expiryMonth: number;
  expiryYear: number;
  cvv: string;
}

/**
 * Payment form for collecting card details.
 *
 * Owns its own field signals and validation message.
 * Emits `paymentSubmit` with validated card data — parent handles the API call.
 * Parent passes `submitting` and `apiError` to reflect in-flight / server errors.
 *
 * Usage:
 *   <app-payment-form [order]="order()" [submitting]="submitting()" [apiError]="paymentError()"
 *                     (paymentSubmit)="onPaymentSubmit($event)" />
 */
@Component( {
  selector: 'app-payment-form',
  standalone: true,
  imports: [ CurrencyPipe ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="bg-white border border-gray-200 rounded-xl shadow-sm p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Payment Details</h2>

      <div class="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-5 text-sm text-blue-700">
        <strong>Test mode:</strong> Use any card number to succeed. Use
        <code class="font-mono bg-blue-100 px-1 rounded">4000000000000002</code> to simulate a decline.
      </div>

      @if (displayError()) {
        <div class="bg-red-50 border border-red-200 rounded-lg p-3 mb-4 text-sm text-red-700"
             role="alert">
          {{ displayError() }}
        </div>
      }

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1" for="cardholder-name">
          Cardholder Name
        </label>
        <input
          id="cardholder-name"
          type="text"
          [value]="cardholderName()"
          (input)="cardholderName.set($any($event.target).value)"
          placeholder="Jane Doe"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2
                 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic"/>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1" for="card-number">
          Card Number
        </label>
        <input
          id="card-number"
          type="text"
          [value]="cardNumber()"
          (input)="cardNumber.set($any($event.target).value)"
          maxlength="19"
          placeholder="4242 4242 4242 4242"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 font-mono focus:outline-none
                 focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic
                 placeholder:font-sans"/>
      </div>

      <div class="flex gap-4 mb-6">
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-1" for="expiry-month">
            Expiry Month
          </label>
          <input
            id="expiry-month"
            type="number"
            [value]="expiryMonth()"
            (input)="expiryMonth.set(+$any($event.target).value)"
            min="1" max="12"
            placeholder="MM"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none
                   focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic"/>
        </div>
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-1" for="expiry-year">
            Expiry Year
          </label>
          <input
            id="expiry-year"
            type="number"
            [value]="expiryYear()"
            (input)="expiryYear.set(+$any($event.target).value)"
            min="2025"
            placeholder="YYYY"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none
                   focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic"/>
        </div>
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-1" for="cvv">CVV</label>
          <input
            id="cvv"
            type="text"
            [value]="cvv()"
            (input)="cvv.set($any($event.target).value)"
            maxlength="4"
            placeholder="123"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 font-mono focus:outline-none
                   focus:ring-2 focus:ring-blue-500 placeholder:text-gray-400 placeholder:italic
                   placeholder:font-sans"/>
        </div>
      </div>

      <button
        type="button"
        (click)="submit()"
        [disabled]="submitting()"
        class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white py-3
               rounded-lg font-semibold transition-colors">
        @if (submitting()) {
          Processing...
        } @else {
          Pay {{ order().total | currency }}
        }
      </button>
    </div>
  `,
} )
export class PaymentFormComponent {
  order = input.required<OrderDetails>();
  submitting = input( false );
  /** API-level error from the parent (e.g. network failure). */
  apiError = input<string | null>( null );

  paymentSubmit = output<CardDetails>();

  // Prefilled with test values — clear these for production
  cardholderName = signal( 'Test User' );
  cardNumber = signal( '4242424242424242' );
  expiryMonth = signal( 12 );
  expiryYear = signal( 2030 );
  cvv = signal( '123' );

  private validationError = signal<string | null>( null );

  /** Validation errors take priority; fall back to API error from parent. */
  displayError = computed( () => this.validationError() ?? this.apiError() );

  submit(): void {
    if ( !this.cardholderName() || !this.cardNumber() || !this.expiryMonth() || !this.expiryYear() || !this.cvv() ) {
      this.validationError.set( 'Please fill in all card details.' );
      return;
    }
    this.validationError.set( null );
    this.paymentSubmit.emit( {
      cardholderName: this.cardholderName(),
      cardNumber: this.cardNumber().replaceAll( ' ', '' ),
      expiryMonth: this.expiryMonth(),
      expiryYear: this.expiryYear(),
      cvv: this.cvv(),
    } );
  }
}
