export interface PaymentDetails {
  id: number;
  orderId: number;
  userId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
}
