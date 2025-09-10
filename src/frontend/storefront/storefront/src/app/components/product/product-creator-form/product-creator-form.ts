import { Component } from '@angular/core';
import { ProductService } from '../../../core/service/product-service';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProductCreationDetails } from '../../../core/model/product-creation-details';

@Component( {
  selector: 'app-product-creator-form',
  imports: [ CommonModule, ReactiveFormsModule ],
  template: `
    <form [formGroup]="productForm" (ngSubmit)="submitProductCreated()">
      <label class="block mt-2">Product Name:</label>
      <input type="text" formControlName="name" class="bg-green-300">

      <label class="block mt-2">Description:</label>
      <input type="text" formControlName="description" class="bg-green-300">

      <label class="block mt-2">SKU</label>
      <input type="text" formControlName="sku" class="bg-green-300">

      <label class="block mt-2">Slug</label>
      <input type="text" formControlName="slug" class="bg-green-300">

      <label class="block mt-2">Brand:</label>
      <input type="text" formControlName="brand" class="bg-green-300">

      <label class="block mt-2">Price:</label>
      <input type="text" formControlName="price" class="bg-green-300">

      <label class="block mt-2">Category:</label>
      <input type="text" formControlName="category" class="bg-green-300">

      <label class="block mt-2">Stock:</label>
      <input type="text" formControlName="stock" class="bg-green-300">

      <button class="block mt-2" type="submit" [disabled]="!productForm.valid">Create</button>
    </form>
  `,
  styles: ``
} )
export class ProductCreatorForm {

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
      price: [ '' ],
      category: [ '' ],
      stock: [ '' ],
    } );
  }

  submitProductCreated() {
    const createdProduct: ProductCreationDetails = this.productForm.value;
    this.productService.createProduct( createdProduct ).subscribe( {
      next: ( response ) => console.log( 'Product created:', response ),
      error: ( err ) => console.error( 'Error creating product:', err ),
    } );
    ;
    console.log( createdProduct );
  }

}
