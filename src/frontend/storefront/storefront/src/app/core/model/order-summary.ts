import { OrderSummaryItem } from './order-summary-item';
import { PaymentDetails } from './payment-details';

export interface OrderSummary {
  id: number;
  userId: string;
  status: string;
  total: number;
  itemCount: number;
  createdAt: string;
  items: OrderSummaryItem[];
  payment: PaymentDetails;
}
