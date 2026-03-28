package dev.agasen.core.cart;

import dev.agasen.api.cart.CartQueryApi;
import dev.agasen.api.cart.read.CartDetails;
import dev.agasen.core.cart.application.read.CartRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartQueryRestService implements CartQueryApi {

   private final CartRetrievalService retrievalService;

   @Override
   public CartDetails getCart() {
      return retrievalService.getCart( userId() );
   }

   private String userId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
