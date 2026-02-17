import { Component, computed, signal } from '@angular/core';
import { ProductService } from '../../core/service/product-service';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductCreationDetails } from '../../core/model/product-creation-details';
import { FormInput } from '../shared/form/form-input';

@Component( {
  selector: 'app-product-creator-form',
  imports: [ CommonModule, ReactiveFormsModule, FormInput ],
  template: `
    @if (isCreating()) {
      <form [formGroup]="productForm" (ngSubmit)="submitProductCreated()">
        <app-form-input type="text" label="Product Name" formControlName="name"/>

        <label class="block mt-2">Description:</label>
        <input type="text" formControlName="description" class="bg-green-300">

        <label class="block mt-2">SKU</label>
        <input type="text" formControlName="sku" class="bg-green-300">

        <label class="block mt-2">Slug</label>
        <input type="text" formControlName="slug" class="bg-green-300">

        <label class="block mt-2">Brand:</label>
        <input
          type="text"
          formControlName="brand"
          (input)="filterSuggestions($event)"
          class="bg-green-300"
        >

        @if (filteredSuggestions().length > 0) {
          <ul class="bg-white border mt-1">
            @for (suggestion of filteredSuggestions(); track suggestion) {
              <li (click)="selectSuggestion(suggestion)"
                  class="cursor-pointer hover:bg-gray-200 px-2 py-1">
                {{ suggestion }}
              </li>
            }
          </ul>
        }

        <label class="block mt-2">Price:</label>
        <input type="number" min="0" formControlName="price" class="bg-green-300">

        <label class="block mt-2">Category:</label>
        <input type="text" formControlName="category" class="bg-green-300">

        <label class="block mt-2">Stock:</label>
        <input type="number" min="0" formControlName="stock" class="bg-green-300">

        <button class="block mt-2" type="submit" [disabled]="!productForm.valid">Create</button>
      </form>
    } @else {
      <button (click)="isCreating.set(true)">Add Product</button>
    }
  `,
  styles: ``
} )
export class ProductCreatorForm {

  isCreating = signal( false );

  private allSuggestions = signal<string[]>( [] );
  private query = signal<string>( '' );

  filteredSuggestions = computed( () =>
    this.allSuggestions().filter( b =>
      b.toLowerCase().includes( this.query().toLowerCase() )
    )
  )

  productForm: FormGroup;

  constructor(
    private productService: ProductService,
    private fb: FormBuilder,
  ) {
    this.productForm = this.createProductForm()
  }

  filterSuggestions( event: Event ) {
    const input = event.target as HTMLInputElement;
    const value = input.value.trim();
    this.query.set( value );

    if ( value.length >= 2 ) {
      this.productService.queryBrands( value )
        .subscribe( b => this.allSuggestions.set( b ) );
    } else {
      this.allSuggestions.set( [] )
    }
  }

  selectSuggestion( suggestion: string ) {
    this.productForm.get( 'brand' )?.setValue( suggestion );
    this.query.set( suggestion );
    this.allSuggestions.set( [] );
  }

  private createProductForm(): FormGroup {
    return this.fb.group( {
      name: [ '' ],
      description: [ '' ],
      sku: [ '' ],
      slug: [ '' ],
      brand: [ '' ],
      price: [ 0, [ Validators.required, Validators.min( 0 ) ] ],
      category: [ '' ],
      stock: [ 0, [ Validators.required, Validators.min( 0 ) ] ],
    } );
  }

  submitProductCreated() {
    const createdProduct: ProductCreationDetails = this.productForm.value;
    this.productService.createProduct( createdProduct ).subscribe( {
      next: ( response ) => console.log( 'Product created:', response ),
      error: ( err ) => console.error( 'Error creating product:', err ),
    } );
    console.log( createdProduct );
  }
}
