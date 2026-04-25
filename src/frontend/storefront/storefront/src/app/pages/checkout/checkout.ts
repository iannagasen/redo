import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { PaymentService } from '../../core/service/payment.service';
import { OrderService } from '../../core/service/order-service';
import { OrderDetails } from '../../core/model/order-details';
import { PaymentFormComponent, CardDetails } from './payment-form.component';
import { PaymentResultComponent } from './payment-result.component';

@Component( {
  selector: 'app-checkout',
  standalone: true,
  imports: [ RouterModule, CurrencyPipe, PaymentFormComponent, PaymentResultComponent ],
  template: `
    <div class="p-5 max-w-2xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Checkout</h1>

      @if (loading()) {
        <div class="text-center py-20 text-gray-500">Loading order details...</div>
      } @else if (!order()) {
        <div class="text-center py-20 text-gray-500">Order not found.</div>
      } @else if (settled()) {
        <app-payment-result
          [status]="order()!.status"
          [orderId]="order()!.id"
          (retry)="onRetry()" />
      } @else {
        <!-- Order summary -->
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-1">Order Summary</h2>
          <p class="text-sm text-gray-500 mb-4">
            Order #{{ order()!.id }} · {{ order()!.itemCount }} item(s)
          </p>
          <div class="flex justify-between font-bold text-gray-900 text-lg border-t border-gray-100 pt-4">
            <span>Total</span>
            <span>{{ order()!.total | currency }}</span>
          </div>
        </div>

        <app-payment-form
          [order]="order()!"
          [submitting]="submitting()"
          [apiError]="paymentError()"
          (paymentSubmit)="onPaymentSubmit($event)" />
      }
    </div>
  `,
} )
export class Checkout implements OnInit {

  order = signal<OrderDetails | null>( null );
  loading = signal( true );
  submitting = signal( false );
  settled = signal( false );
  paymentError = signal<string | null>( null );

  private orderId!: number;
  private idempotencyKey = crypto.randomUUID();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private orderService: OrderService,
  ) {}

  ngOnInit(): void {
    this.orderId = Number( this.route.snapshot.queryParamMap.get( 'orderId' ) );
    if ( !this.orderId ) {
      this.router.navigate( [ '/orders' ] );
      return;
    }
    this.orderService.getOrderById( this.orderId ).subscribe( {
      next: order => { this.order.set( order ); this.loading.set( false ); },
      error: () => this.loading.set( false ),
    } );
  }

  onPaymentSubmit( card: CardDetails ): void {
    const ord = this.order();
    if ( !ord ) return;

    this.submitting.set( true );
    this.paymentError.set( null );

    this.paymentService.initiatePayment(
      { orderId: ord.id, amount: ord.total, currency: 'USD', ...card },
      this.idempotencyKey,
    ).subscribe( {
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

  onRetry(): void {
    // Regenerate key so the backend treats this as a new payment attempt.
    this.idempotencyKey = crypto.randomUUID();
    this.settled.set( false );
    this.paymentError.set( null );
    // PaymentFormComponent is recreated by @if, so card fields reset to defaults automatically.
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
}
