import { Injectable } from '@angular/core';
import { OauthService } from '../../pages/login/oauth-service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, Observable } from 'rxjs';

@Injectable( {
  providedIn: 'root'
} )
export class ProductService {

  private readonly BASE_URL = 'http://localhost:8081/api/v1';

  constructor(
    private oauthService: OauthService,
    private httpClient: HttpClient
  ) {
  }

  getAllProducts(): Observable<ProductDetails[]> {
    const headers = this.createAuthHeaders();
    return this.httpClient.get<any>( `${ this.BASE_URL }/products`, { headers } )
      .pipe(
        map( response => response.content ),
      );
  }

  createProduct( product: ProductCreationDetails ): Observable<void> {
    const headers = this.createAuthHeaders();
    return this.httpClient.post<void>( `${ this.BASE_URL }/products`, product, { headers } );
  }

  private createAuthHeaders(): HttpHeaders {
    const token = this.oauthService.getAccessToken();
    return new HttpHeaders( {
      'Authorization': `Bearer ${ token }`,
      'Content-Type': 'application/json'
    } )
  }
}
