export interface ProductDetails {
  id: number;
  name: string;
  description?: string;
  sku?: string;
  slug?: string;
  brand?: string;
  price?: number;
  currency?: string;
  stock?: number;
  bought?: number;
  cart?: number;
}
