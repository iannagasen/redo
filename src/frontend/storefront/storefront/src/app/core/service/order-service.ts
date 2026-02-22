import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OauthService } from './oauth-service';
import { OrderDetails } from '../model/order-details';
import { CreateOrderRequest } from '../model/create-order-request';
import { CartItemDetails } from '../model/cart-item-details';

@Injectable( { providedIn: 'root' } )
export class OrderService {

  private readonly BASE_URL = 'http://shopbuddy.com/order/api/v1/orders';

  constructor(
    private oauthService: OauthService,
    private http: HttpClient,
  ) {
  }

  createOrder( cartItems: CartItemDetails[] ): Observable<OrderDetails> {
    const request: CreateOrderRequest = {
      items: cartItems.map( item => ( {
        productId: item.productId,
        productName: item.productName,
        brand: item.brand,
        price: item.price,
        currency: item.currency,
        quantity: item.quantity,
      } ) ),
    };
    return this.http.post<OrderDetails>( this.BASE_URL, request, { headers: this.authHeaders() } );
  }

  getOrders(): Observable<OrderDetails[]> {
    return this.http.get<OrderDetails[]>( this.BASE_URL, { headers: this.authHeaders() } );
  }

  getOrderById( id: number ): Observable<OrderDetails> {
    return this.http.get<OrderDetails>( `${ this.BASE_URL }/${ id }`, { headers: this.authHeaders() } );
  }

  private authHeaders(): HttpHeaders {
    const token = this.oauthService.getAccessToken();
    return new HttpHeaders( {
      'Authorization': `Bearer ${ token }`,
      'Content-Type': 'application/json',
    } );
  }
}
