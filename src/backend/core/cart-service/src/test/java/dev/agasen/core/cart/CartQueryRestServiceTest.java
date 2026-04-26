package dev.agasen.core.cart;

import dev.agasen.platform.contracts.core.cart.read.CartDetails;
import dev.agasen.platform.contracts.core.cart.read.CartItemDetails;
import dev.agasen.core.cart.application.read.CartRetrievalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
class CartQueryRestServiceTest {

   @Mock private CartRetrievalService retrievalService;
   @Mock private SecurityContext securityContext;
   @Mock private Authentication authentication;

   @InjectMocks private CartQueryRestService cartQueryRestService;

   @BeforeEach
   void setUpSecurityContext() {
      when( securityContext.getAuthentication() ).thenReturn( authentication );
      when( authentication.getName() ).thenReturn( "user-123" );
      SecurityContextHolder.setContext( securityContext );
   }

   @AfterEach
   void clearSecurityContext() {
      SecurityContextHolder.clearContext();
   }

   @Test
   @DisplayName( "getCart delegates to CartRetrievalService with userId from security context" )
   void getCart_delegatesToRetrievalService_withUserIdFromSecurityContext() {
      CartDetails expected = buildCartDetails( "user-123" );
      when( retrievalService.getCart( "user-123" ) ).thenReturn( expected );

      CartDetails result = cartQueryRestService.getCart();

      assertThat( result ).isEqualTo( expected );
      verify( retrievalService ).getCart( "user-123" );
   }

   @Test
   @DisplayName( "getCart returns empty cart when user has no items" )
   void getCart_returnsEmptyCart_whenUserHasNoItems() {
      CartDetails emptyCart = buildEmptyCartDetails( "user-456" );
      when( authentication.getName() ).thenReturn( "user-456" );
      when( retrievalService.getCart( "user-456" ) ).thenReturn( emptyCart );

      CartDetails result = cartQueryRestService.getCart();

      assertThat( result.getUserId() ).isEqualTo( "user-456" );
      assertThat( result.getItems() ).isEmpty();
      assertThat( result.getTotal() ).isEqualByComparingTo( BigDecimal.ZERO );
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private CartDetails buildCartDetails( String userId ) {
      CartItemDetails item = new CartItemDetails();
      item.setProductId( 1L );
      item.setProductName( "Widget" );
      item.setPrice( new BigDecimal( "9.99" ) );
      item.setQuantity( 2 );
      item.setLineTotal( new BigDecimal( "19.98" ) );

      CartDetails details = new CartDetails();
      details.setUserId( userId );
      details.setItems( List.of( item ) );
      details.setTotal( new BigDecimal( "19.98" ) );
      details.setItemCount( 2 );
      return details;
   }

   private CartDetails buildEmptyCartDetails( String userId ) {
      CartDetails details = new CartDetails();
      details.setUserId( userId );
      details.setItems( List.of() );
      details.setTotal( BigDecimal.ZERO );
      details.setItemCount( 0 );
      return details;
   }
}
