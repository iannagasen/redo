import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderDetails } from '../model/order-details';
import { OrderSummary } from '../model/order-summary';
import { CreateOrderRequest } from '../model/create-order-request';
import { CartItemDetails } from '../model/cart-item-details';

@Injectable( { providedIn: 'root' } )
export class OrderService {

  private readonly BASE_URL = 'http://shopbuddy.com/order/api/v1/orders';

  constructor( private http: HttpClient ) {}

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
    return this.http.post<OrderDetails>( this.BASE_URL, request );
  }

  getOrders(): Observable<OrderDetails[]> {
    return this.http.get<OrderDetails[]>( this.BASE_URL );
  }

  getOrderById( id: number ): Observable<OrderDetails> {
    return this.http.get<OrderDetails>( `${ this.BASE_URL }/${ id }` );
  }

  getOrderSummary( id: number ): Observable<OrderSummary> {
    return this.http.get<OrderSummary>( `${ this.BASE_URL }/${ id }/summary` );
  }
}
