export interface CartItemDetails {
  productId: number;
  productName: string;
  brand?: string;
  price: number;
  currency?: string;
  quantity: number;
  lineTotal: number;
}
