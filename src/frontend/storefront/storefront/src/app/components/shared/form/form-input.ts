import { Component, Input, Optional } from '@angular/core';
import { ControlContainer, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component( {
  selector: 'app-form-input',
  imports: [
    CommonModule, ReactiveFormsModule
  ],
  template: `
    <label class="block mt-2">{{ label }}</label>
    <input
      [type]="type"
      [formControlName]="formControlName"
      class="bg-green-300"
    >

    @if (control?.invalid && control?.touched) {
      <div>
        <small class="text-red-400">
          {{ getErrorMessage() }}
        </small>
      </div>
    }
  `,
  styles: ``
} )
export class FormInput {
  @Input() label!: string;
  @Input() formControlName!: string;
  @Input() type!: string;

  private readonly defaultMessages: { [ key: string ]: ( err?: any ) => string } = {
    required: () => `${ this.label } is required`,
    min: ( err ) => `${ this.label } must be at least ${ err.min }`,
    max: ( err ) => `${ this.label } must not exceed ${ err.max }`,
    minlength: ( err ) => `${ this.label } must be at least ${ err.minlength }`,
    maxlength: ( err ) => `${ this.label } must not exceed ${ err.maxlength }`,
  };

  constructor(
    @Optional() private controlContainer: ControlContainer
  ) {
  }

  get control(): FormControl | null {
    if ( this.controlContainer ) {
      return this.controlContainer.control?.get( this.formControlName ) as FormControl;
    } else {
      return null;
    }
  }

  getErrorMessage(): string {
    if ( !this.control || !this.control.errors ) return '';

    const errors = this.control.errors;

    for ( const key in Object.keys( errors ) ) {
      if ( this.defaultMessages[ key ] ) {
        return this.defaultMessages[ key ]( errors[ key ] );
      }
    }

    return '';
  }
}
