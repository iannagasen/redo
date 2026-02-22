import { OrderItemDetails } from './order-item-details';

export interface OrderDetails {
  id: number;
  userId: string;
  status: string;
  total: number;
  itemCount: number;
  items: OrderItemDetails[];
  createdAt: string;
}
