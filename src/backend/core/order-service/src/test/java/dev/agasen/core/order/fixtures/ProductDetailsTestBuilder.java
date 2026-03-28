package dev.agasen.core.order.fixtures;

import dev.agasen.api.core.product.product.ProductDetails;

public class ProductDetailsTestBuilder {

   public static ProductDetails productDetails( Long id, String description ) {
      var product = new ProductDetails();
      product.setId( id );
      product.setDescription( description );
      return product;
   }
}
