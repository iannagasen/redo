import { Component, signal } from '@angular/core';
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

        <!--        <label class="block mt-2">Product Name:</label>-->
        <!--        <input type="text" formControlName="name" class="bg-green-300">-->
        <!--        @if (productForm.get("name")?.invalid && productForm.get("name")?.touched) {-->
        <!--          <div>-->
        <!--            <small class="text-red-400">Product Name is required</small>-->
        <!--          </div>-->
        <!--        }-->

        <label class="block mt-2">Description:</label>
        <input type="text" formControlName="description" class="bg-green-300">

        <label class="block mt-2">SKU</label>
        <input type="text" formControlName="sku" class="bg-green-300">

        <label class="block mt-2">Slug</label>
        <input type="text" formControlName="slug" class="bg-green-300">

        <label class="block mt-2">Brand:</label>
        <input type="text" formControlName="brand" class="bg-green-300">

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

  productForm: FormGroup;

  constructor(
    private productService: ProductService,
    private fb: FormBuilder,
  ) {
    this.productForm = this.createProductForm()
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
