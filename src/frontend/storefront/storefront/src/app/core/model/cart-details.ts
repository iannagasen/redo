import { CartItemDetails } from './cart-item-details';

export interface CartDetails {
  userId: string;
  items: CartItemDetails[];
  total: number;
  itemCount: number;
}
