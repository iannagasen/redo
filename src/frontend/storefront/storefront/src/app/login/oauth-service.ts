import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable( {
  providedIn: 'root'
} )
export class OauthService {
  private clientId = 'angular-client';
  private redirectUri = 'http://localhost:4200/login/callback';
  private authServer = 'http://localhost:8080';

  constructor( private http: HttpClient ) {
  }

  private base64UrlEncode( buffer: ArrayBuffer ): string {
    return btoa( String.fromCharCode( ...new Uint8Array( buffer ) ) )
      .replace( /\+/g, '-' )
      .replace( /\//g, '_' )
      .replace( /=+$/, '' );
  }

  private generateCodeVerifier( length = 128 ): string {
    const array = new Uint8Array( length );
    crypto.getRandomValues( array );
    return Array
      .from( array, dec => ( '0' + dec.toString( 16 ) )
        .slice( -2 ) )
      .join( '' );
  }

  private async generateCodeChallenge( verifier: string ): Promise<string> {
    const data = new TextEncoder().encode( verifier );
    const digest = await crypto.subtle.digest( 'SHA-256', data );
    return this.base64UrlEncode( digest );
  }

  async login() {
    const verifier = this.generateCodeVerifier();
    sessionStorage.setItem( 'pkce_verifier', verifier );

    const challenge = await this.generateCodeChallenge( verifier );

    const authorizeUrl = `${ this.authServer }/oauth2/authorize` +
      `?client_id=${ this.clientId }` +
      `&response_type=code` +
      `&redirect_uri=${ encodeURIComponent( this.redirectUri ) }` +
      `&scope=openid` +
      `&code_challenge=${ challenge }` +
      `&code_challenge_method=S256`;

    window.location.href = authorizeUrl;
  }

  exchangeCodeForToken( code: string ) {
    const verifier = sessionStorage.getItem( 'pkce_verifier' );

    const body = new URLSearchParams();
    body.set( 'grant_type', 'authorization_code' );
    body.set( 'code', code );
    body.set( 'redirect_uri', this.redirectUri );
    body.set( 'client_id', this.clientId );
    body.set( 'code_verifier', verifier! );

    return this.http.post<any>( `${ this.authServer }/oauth2/token`, body.toString(), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    } );
  }

  storeTokens( tokens: any ) {
    console.log( tokens );
    sessionStorage.setItem( 'access_token', tokens.access_token );
    if ( tokens.id_token ) {
      sessionStorage.setItem( 'id_token', tokens.id_token );
    }
    const expiryTime = Date.now() + ( tokens.expires_in * 1000 )
    sessionStorage.setItem( 'expiry', expiryTime.toString() );
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem( 'access_token' );
  }

  isLoggedIn(): boolean {
    const token = sessionStorage.getItem( 'access_token' );
    const expiry = sessionStorage.getItem( 'expiry' );

    if ( !token || !expiry ) return false;

    return Date.now() < parseInt( expiry, 10 );
  }

  logout(): void {
    sessionStorage.removeItem( 'access_token' );
    sessionStorage.removeItem( 'id_token' );
    sessionStorage.removeItem( 'pkce_verifier' );
  }
}
