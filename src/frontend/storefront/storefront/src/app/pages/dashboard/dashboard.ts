import { Component, OnInit, signal } from '@angular/core';
import { OauthService } from '../../core/service/oauth-service';
import { Router, RouterModule } from '@angular/router';
import { ProductService } from '../../core/service/product-service';
import { ProductCreatorForm } from '../../components/product/product-creator-form';
import { ProductCard } from '../../components/product/product-card';
import { ProductDetails } from '../../core/model/product-details';

@Component( {
  selector: 'app-dashboard',
  imports: [ RouterModule, ProductCreatorForm, ProductCard ],
  template: `
    <div class="p-5 max-w-7xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Dashboard</h1>

      <div class="flex justify-between items-center bg-gray-50 p-4 rounded-lg mb-8">
        <p class="text-gray-700">Welcome back! You are logged in.</p>
        <button (click)="logout()"
                class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
          Logout
        </button>
      </div>

      <section>
        <h2 class="text-2xl font-semibold text-gray-800 mb-6">Products</h2>

        @if (loading()) {
          <div class="text-center py-10 text-gray-600">
            <div class="animate-spin inline-block w-6 h-6 border-2 border-current border-t-transparent
                        text-blue-600 rounded-full mb-2"></div>
            <p>Loading products...</p>
          </div>
        } @else if (error()) {
          <div class="text-center py-10 text-red-600">
            <p class="mb-4">{{ error() }}</p>
            <button (click)="loadProducts()"
                    class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
              Retry
            </button>
          </div>
        } @else if (products().length === 0) {
          <div class="text-center py-10 text-gray-600">
            <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
            </svg>
            <p>No products found.</p>
          </div>
        } @else {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            @for (product of products(); track product.id) {
              <a [routerLink]="['/products', product.id]">
                <app-product-card [product]="product" />
              </a>
            }
          </div>
        }
      </section>

      <div class="mt-8">
        <app-product-creator-form />
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
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set( true );
    this.error.set( null );

    this.productService.getAllProducts().subscribe( {
      next: products => {
        this.products.set( products );
        this.loading.set( false );
      },
      error: err => {
        this.error.set( 'Failed to load products. Please try again.' );
        this.loading.set( false );
        if ( err.status === 401 ) {
          this.auth.logout();
          this.router.navigate( [ '/login' ] );
        }
      },
    } );
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate( [ '/login' ] );
  }
}
