import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { ProductCreationDetails } from '../model/product-creation-details';
import { ProductDetails } from '../model/product-details';

@Injectable( { providedIn: 'root' } )
export class ProductService {

  private readonly BASE_URL = 'http://shopbuddy.com/product/api/v1';

  constructor( private httpClient: HttpClient ) {}

  getAllProducts(): Observable<ProductDetails[]> {
    return this.httpClient.get<any>( `${ this.BASE_URL }/products` )
      .pipe( map( response => response.content ) );
  }

  getProductById( id: number ): Observable<ProductDetails> {
    return this.httpClient.get<ProductDetails>( `${ this.BASE_URL }/products/${ id }` );
  }

  createProduct( product: ProductCreationDetails ): Observable<void> {
    return this.httpClient.post<void>( `${ this.BASE_URL }/products`, product );
  }

  queryBrands( query: string ): Observable<string[]> {
    return this.httpClient.get<string[]>( `${ this.BASE_URL }/products/brands?q=${ query }&p=0&s=5` );
  }
}
