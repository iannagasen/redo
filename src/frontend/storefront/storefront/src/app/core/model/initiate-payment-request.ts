export interface InitiatePaymentRequest {
  orderId: number;
  amount: number;
  currency: string;
  cardNumber: string;
  cardholderName: string;
  expiryMonth: number;
  expiryYear: number;
  cvv: string;
}
