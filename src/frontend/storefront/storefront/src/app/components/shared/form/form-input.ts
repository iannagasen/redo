import { Component, Input, Optional } from '@angular/core';
import { ControlContainer, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component( {
  selector: 'app-form-input',
  standalone: true,
  imports: [ CommonModule, ReactiveFormsModule ],
  template: `
    <label class="block mt-2">{{ label }}</label>
    <input
      [type]="type"
      [formControl]="control"
      class="bg-green-300"
    >

    @if (control?.invalid && control?.touched) {
      <div>
        <small class="text-red-400">
          {{ getErrorMessage() }}
        </small>
      </div>
    }
  `
} )
export class FormInput {
  @Input() label!: string;
  @Input() type: string = 'text';
  @Input() formControlName?: string;
  @Input() formControl?: FormControl;

  private readonly defaultMessages: { [ key: string ]: ( err?: any ) => string } = {
    required: () => `${ this.label } is required`,
    min: ( err ) => `${ this.label } must be at least ${ err.min }`,
    max: ( err ) => `${ this.label } must not exceed ${ err.max }`,
    minlength: ( err ) => `${ this.label } must be at least ${ err.requiredLength } characters`,
    maxlength: ( err ) => `${ this.label } must not exceed ${ err.requiredLength } characters`,
  };

  constructor( @Optional() private controlContainer: ControlContainer ) {
  }

  get control(): FormControl {
    if ( this.formControl ) {
      return this.formControl;
    }
    if ( this.formControlName && this.controlContainer ) {
      return this.controlContainer.control?.get( this.formControlName ) as FormControl;
    }
    throw new Error( `No FormControl found for ${ this.label }` );
  }

  getErrorMessage(): string {
    if ( !this.control || !this.control.errors ) return '';

    const errors = this.control.errors;
    for ( const key of Object.keys( errors ) ) {
      if ( this.defaultMessages[ key ] ) {
        return this.defaultMessages[ key ]( errors[ key ] );
      }
    }
    return '';
  }
}
