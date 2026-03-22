export interface OrderSummaryItem {
  productId: number;
  productName: string;
  brand?: string;
  description?: string;
  price: number;
  currency?: string;
  quantity: number;
  lineTotal: number;
}
