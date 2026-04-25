import { Pipe, PipeTransform } from '@angular/core';

/** Maps a payment status string to its Tailwind badge classes. */
@Pipe( { name: 'paymentStatus', standalone: true } )
export class PaymentStatusPipe implements PipeTransform {
  transform( status: string ): string {
    switch ( status ) {
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'FAILED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }
}
