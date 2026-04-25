import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, takeWhile, timer } from 'rxjs';
import { PaymentDetails } from '../model/payment-details';
import { InitiatePaymentRequest } from '../model/initiate-payment-request';
import { OrderDetails } from '../model/order-details';
import { OrderService } from './order-service';

@Injectable( { providedIn: 'root' } )
export class PaymentService {

  private readonly BASE_URL = 'http://shopbuddy.com/payment/api/v1/payments';

  constructor(
    private http: HttpClient,
    private orderService: OrderService,
  ) {}

  initiatePayment( request: InitiatePaymentRequest, idempotencyKey: string ): Observable<PaymentDetails> {
    return this.http.post<PaymentDetails>( this.BASE_URL, request, {
      // Auth header is added by authInterceptor; only the idempotency key needs to be explicit here.
      headers: { 'Idempotency-Key': idempotencyKey },
    } );
  }

  /** Polls order status every 2 seconds until it leaves PENDING (up to ~30 s). */
  pollOrderUntilSettled( orderId: number ): Observable<OrderDetails> {
    return timer( 0, 2000 ).pipe(
      switchMap( () => this.orderService.getOrderById( orderId ) ),
      takeWhile( order => order.status === 'PENDING', true ),
    );
  }
}
