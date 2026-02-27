import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, timer, switchMap, takeWhile, map } from 'rxjs';
import { OauthService } from './oauth-service';
import { PaymentDetails } from '../model/payment-details';
import { InitiatePaymentRequest } from '../model/initiate-payment-request';
import { OrderDetails } from '../model/order-details';
import { OrderService } from './order-service';

@Injectable( { providedIn: 'root' } )
export class PaymentService {

  private readonly BASE_URL = 'http://shopbuddy.com/payment/api/v1/payments';

  constructor(
    private oauthService: OauthService,
    private http: HttpClient,
    private orderService: OrderService,
  ) {
  }

  initiatePayment( request: InitiatePaymentRequest ): Observable<PaymentDetails> {
    return this.http.post<PaymentDetails>( this.BASE_URL, request, { headers: this.authHeaders() } );
  }

  /**
   * Polls order status every 2 seconds until it leaves PENDING (up to 30s).
   */
  pollOrderUntilSettled( orderId: number ): Observable<OrderDetails> {
    return timer( 0, 2000 ).pipe(
      switchMap( () => this.orderService.getOrderById( orderId ) ),
      takeWhile( order => order.status === 'PENDING', true ),
    );
  }

  private authHeaders(): HttpHeaders {
    const token = this.oauthService.getAccessToken();
    return new HttpHeaders( {
      'Authorization': `Bearer ${ token }`,
      'Content-Type': 'application/json',
    } );
  }
}
