export interface AddToCartRequest {
  productId: number;
  productName: string;
  brand?: string;
  price: number;
  currency?: string;
  quantity: number;
}
