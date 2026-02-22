export interface OrderItemRequest {
  productId: number;
  productName: string;
  brand?: string;
  price: number;
  currency?: string;
  quantity: number;
}

export interface CreateOrderRequest {
  items: OrderItemRequest[];
}
