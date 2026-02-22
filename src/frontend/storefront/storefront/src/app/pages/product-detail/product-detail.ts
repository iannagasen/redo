import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../core/service/product-service';
import { CartService } from '../../core/service/cart-service';
import { ProductDetails } from '../../core/model/product-details';
import { AddToCartRequest } from '../../core/model/add-to-cart-request';

@Component( {
  selector: 'app-product-detail',
  imports: [ CommonModule, RouterModule ],
  template: `
    <div class="p-5 max-w-4xl mx-auto">

      <button
        (click)="goBack()"
        class="flex items-center text-blue-600 hover:text-blue-800 mb-6 text-sm font-medium transition-colors">
        <svg class="w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Back to Products
      </button>

      @if (loading()) {
        <div class="text-center py-20 text-gray-600">
          <div
            class="animate-spin inline-block w-8 h-8 border-2 border-current border-t-transparent text-blue-600 rounded-full mb-3">
          </div>
          <p>Loading product...</p>
        </div>
      }

      @if (error()) {
        <div class="text-center py-20">
          <svg class="mx-auto h-12 w-12 text-red-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <p class="text-red-600 mb-4">{{ error() }}</p>
          <button
            (click)="loadProduct()"
            class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
            Retry
          </button>
        </div>
      }

      @if (!loading() && !error() && product()) {
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">

          <!-- Header -->
          <div class="bg-gray-50 px-8 py-6 border-b border-gray-200">
            <div class="flex items-start justify-between">
              <div>
                <h1 class="text-3xl font-bold text-gray-900 mb-1">{{ product()!.name }}</h1>
                @if (product()!.brand) {
                  <p class="text-sm text-gray-500 uppercase tracking-wide font-medium">{{ product()!.brand }}</p>
                }
              </div>
              @if (product()!.price) {
                <div class="text-right">
                  <p class="text-3xl font-bold text-green-600">{{ product()!.price | currency: (product()!.currency || 'USD') }}</p>
                </div>
              }
            </div>
          </div>

          <!-- Body -->
          <div class="px-8 py-6 space-y-6">

            @if (product()!.description) {
              <div>
                <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-2">Description</h2>
                <p class="text-gray-700 leading-relaxed">{{ product()!.description }}</p>
              </div>
            }

            <!-- Metadata grid -->
            <div class="grid grid-cols-2 md:grid-cols-3 gap-4">

              @if (product()!.sku) {
                <div class="bg-gray-50 rounded-lg p-4">
                  <p class="text-xs text-gray-500 uppercase tracking-wide mb-1">SKU</p>
                  <p class="text-gray-900 font-mono text-sm">{{ product()!.sku }}</p>
                </div>
              }

              @if (product()!.slug) {
                <div class="bg-gray-50 rounded-lg p-4">
                  <p class="text-xs text-gray-500 uppercase tracking-wide mb-1">Slug</p>
                  <p class="text-gray-900 font-mono text-sm">{{ product()!.slug }}</p>
                </div>
              }

              <div class="bg-gray-50 rounded-lg p-4">
                <p class="text-xs text-gray-500 uppercase tracking-wide mb-1">Stock</p>
                <p class="font-semibold" [class]="(product()!.stock ?? 0) > 0 ? 'text-green-600' : 'text-red-500'">
                  {{ (product()!.stock ?? 0) > 0 ? (product()!.stock + ' available') : 'Out of stock' }}
                </p>
              </div>

              @if ((product()!.bought ?? 0) > 0) {
                <div class="bg-gray-50 rounded-lg p-4">
                  <p class="text-xs text-gray-500 uppercase tracking-wide mb-1">Times Purchased</p>
                  <p class="text-gray-900 font-semibold">{{ product()!.bought }}</p>
                </div>
              }

            </div>

            <!-- Action -->
            <div class="pt-2 flex items-center gap-4 flex-wrap">
              <button
                (click)="addToCart()"
                [disabled]="(product()!.stock ?? 0) === 0"
                class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed
                       text-white px-8 py-3 rounded-lg font-semibold transition-colors">
                {{ (product()!.stock ?? 0) > 0 ? 'Add to Cart' : 'Out of Stock' }}
              </button>

              @if (addedToCart()) {
                <div class="flex items-center gap-2 text-green-600 font-medium">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                  Added to cart!
                  <a [routerLink]="['/cart']" class="underline hover:text-green-800 transition-colors">View cart</a>
                </div>
              }
            </div>

          </div>
        </div>
      }

    </div>
  `,
  styles: ``
} )
export class ProductDetail implements OnInit {

  product = signal<ProductDetails | null>( null );
  loading = signal( false );
  error = signal<string | null>( null );
  addedToCart = signal( false );

  private productId!: number;
  private addedTimer?: ReturnType<typeof setTimeout>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
  ) {
  }

  ngOnInit() {
    this.productId = Number( this.route.snapshot.paramMap.get( 'id' ) );
    this.loadProduct();
  }

  loadProduct() {
    this.loading.set( true );
    this.error.set( null );

    this.productService.getProductById( this.productId ).subscribe( {
      next: ( product ) => {
        this.product.set( product );
        this.loading.set( false );
      },
      error: ( err ) => {
        this.error.set( err.status === 404 ? 'Product not found.' : 'Failed to load product. Please try again.' );
        this.loading.set( false );
      }
    } );
  }

  addToCart(): void {
    const p = this.product();
    if ( !p ) return;
    const req: AddToCartRequest = {
      productId: p.id,
      productName: p.name,
      brand: p.brand,
      price: p.price ?? 0,
      currency: p.currency,
      quantity: 1,
    };
    this.cartService.addToCart( req );
    this.addedToCart.set( true );
    clearTimeout( this.addedTimer );
    this.addedTimer = setTimeout( () => this.addedToCart.set( false ), 3000 );
  }

  goBack() {
    this.router.navigate( [ '/dashboard' ] );
  }
}
