import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { CartDetails } from '../model/cart-details';
import { CartItemDetails } from '../model/cart-item-details';
import { AddToCartRequest } from '../model/add-to-cart-request';

const EMPTY_CART: CartDetails = { userId: '', items: [], total: 0, itemCount: 0 };

@Injectable( { providedIn: 'root' } )
export class CartService {

  private readonly BASE_URL = 'http://shopbuddy.com/cart/api/v1';

  private _cart = signal<CartDetails>( EMPTY_CART );

  readonly cartItems = computed( () => this._cart().items );
  readonly itemCount = computed( () => this._cart().itemCount );
  readonly total = computed( () => this._cart().total );

  constructor( private http: HttpClient ) {}

  loadCart(): Observable<CartDetails> {
    return this.http.get<CartDetails>( `${ this.BASE_URL }/cart` )
      .pipe( tap( cart => this._cart.set( cart ) ) );
  }

  addToCart( req: AddToCartRequest ): void {
    this.http.post<CartDetails>( `${ this.BASE_URL }/cart/items`, req )
      .subscribe( { next: cart => this._cart.set( cart ) } );
  }

  updateQuantity( productId: number, quantity: number ): void {
    if ( quantity <= 0 ) {
      this.removeFromCart( productId );
      return;
    }
    this.http.put<CartDetails>( `${ this.BASE_URL }/cart/items/${ productId }`, { quantity } )
      .subscribe( { next: cart => this._cart.set( cart ) } );
  }

  removeFromCart( productId: number ): void {
    this.http.delete<void>( `${ this.BASE_URL }/cart/items/${ productId }` )
      .subscribe( { next: () => this.loadCart().subscribe() } );
  }

  clearCart(): void {
    this.http.delete<void>( `${ this.BASE_URL }/cart` )
      .subscribe( { next: () => this._cart.set( EMPTY_CART ) } );
  }

  isInCart( productId: number ): boolean {
    return this._cart().items.some( ( item: CartItemDetails ) => item.productId === productId );
  }
}
