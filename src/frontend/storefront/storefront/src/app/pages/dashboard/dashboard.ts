import { Component, OnInit, signal } from '@angular/core';
import { OauthService } from '../../core/service/oauth-service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../core/service/product-service';
import { ProductCreatorForm } from '../../components/product/product-creator-form';

@Component( {
  selector: 'app-dashboard',
  imports: [ CommonModule, ProductCreatorForm ],
  template: `
    <div class="p-5 max-w-7xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Dashboard</h1>

      <div class="flex justify-between items-center bg-gray-50 p-4 rounded-lg mb-8">
        <p class="text-gray-700">Welcome back! You are logged in.</p>
        <button
          (click)="logout()"
          class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
          Logout
        </button>
      </div>

      <div class="mt-5">
        <h2 class="text-2xl font-semibold text-gray-800 mb-6">Products</h2>

        @if (loading()) {
          <div class="text-center py-10 text-gray-600">
            <div
              class="animate-spin inline-block w-6 h-6 border-2 border-current border-t-transparent text-blue-600 rounded-full mb-2">
            </div>
            <p>Loading products...</p>
          </div>
        }

        @if (error()) {
          <div class="text-center py-10 text-red-600">
            <p class="mb-4">Error loading products: {{ error() }}</p>
            <button
              (click)="loadProducts()"
              class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
              Retry
            </button>
          </div>
        }

        @if (!loading() && !error() && products().length === 0) {
          <div class="text-center py-10 text-gray-600">
            <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
            </svg>
            <p>No products found.</p>
          </div>
        }

        @if (!loading() && !error() && products().length > 0) {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mt-5">
            @for (product of products(); track product.id) {
              <div
                class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200">
                <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ product.name }}</h3>

                @if (product.description) {
                  <p class="text-gray-600 mb-3 text-sm leading-relaxed">
                    {{ product.description }}
                  </p>
                }

                @if (product.price) {
                  <p class="text-lg font-bold text-green-600 mb-2">
                    {{ product.price | currency }}
                  </p>
                }

                @if (product.category) {
                  <p class="text-xs text-gray-500 uppercase tracking-wide">
                    {{ product.category }}
                  </p>
                }
              </div>
            }
          </div>
        }
      </div>

      <div class="mt-3">
        <app-product-creator-form></app-product-creator-form>
      </div>
    </div>
  `,
  styles: ``
} )
export class Dashboard implements OnInit {

  products = signal<ProductDetails[]>( [] );
  loading = signal( false );
  error = signal<string | null>( null );

  constructor(
    private auth: OauthService,
    private router: Router,
    private productService: ProductService,
  ) {
  }

  ngOnInit() {
    // Check if user is logged in, if not redirect to login
    if ( !this.auth.isLoggedIn() ) {
      this.router.navigate( [ '/login' ] );
      return;
    }

    // Load products on component initialization
    this.loadProducts();
  }

  loadProducts() {
    this.loading.set( true );
    this.error.set( null );

    this.productService.getAllProducts()
      .subscribe( {
        next: ( response ) => {
          this.products.set( response );  // <-- set the product array
          this.loading.set( false );
        },
        error: ( error ) => {
          this.error.set( 'Failed to load products. Please try again.' );
          this.loading.set( false );
          if ( error.status === 401 ) {
            this.auth.logout();
            this.router.navigate( [ '/login' ] );
          }
        }
      } );
  }

  createProduct() {

  }

  logout() {
    this.auth.logout();
    this.router.navigate( [ '/login' ] );
  }
}
