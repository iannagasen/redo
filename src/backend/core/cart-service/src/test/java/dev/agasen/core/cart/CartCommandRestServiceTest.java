package dev.agasen.core.cart;

import dev.agasen.platform.contracts.core.cart.read.CartDetails;
import dev.agasen.platform.contracts.core.cart.read.CartItemDetails;
import dev.agasen.platform.contracts.core.cart.write.AddCartItemRequest;
import dev.agasen.platform.contracts.core.cart.write.UpdateCartItemRequest;
import dev.agasen.core.cart.application.write.CartCommandService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
class CartCommandRestServiceTest {

   @Mock private CartCommandService commandService;
   @Mock private SecurityContext securityContext;
   @Mock private Authentication authentication;

   @InjectMocks private CartCommandRestService cartCommandRestService;

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
   @DisplayName( "addItem delegates to CartCommandService with userId from security context" )
   void addItem_delegatesToCommandService_withUserIdFromSecurityContext() {
      AddCartItemRequest req = buildAddItemRequest( 1L, "Widget", 2 );
      CartDetails expected = buildCartDetails( "user-123" );
      when( commandService.addItem( "user-123", req ) ).thenReturn( expected );

      CartDetails result = cartCommandRestService.addItem( req );

      assertThat( result ).isEqualTo( expected );
      verify( commandService ).addItem( "user-123", req );
   }

   @Test
   @DisplayName( "updateQuantity delegates to CartCommandService with userId and productId" )
   void updateQuantity_delegatesToCommandService_withUserIdAndProductId() {
      UpdateCartItemRequest req = new UpdateCartItemRequest();
      req.setQuantity( 5 );
      CartDetails expected = buildCartDetails( "user-123" );
      when( commandService.updateItemQuantity( "user-123", 1L, 5 ) ).thenReturn( expected );

      CartDetails result = cartCommandRestService.updateQuantity( 1L, req );

      assertThat( result ).isEqualTo( expected );
      verify( commandService ).updateItemQuantity( "user-123", 1L, 5 );
   }

   @Test
   @DisplayName( "removeItem delegates to CartCommandService and returns 204 No Content" )
   void removeItem_delegatesToCommandService_andReturns204() {
      doNothing().when( commandService ).removeItem( "user-123", 1L );

      ResponseEntity< Void > response = cartCommandRestService.removeItem( 1L );

      assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.NO_CONTENT );
      verify( commandService ).removeItem( "user-123", 1L );
   }

   @Test
   @DisplayName( "clearCart delegates to CartCommandService and returns 204 No Content" )
   void clearCart_delegatesToCommandService_andReturns204() {
      doNothing().when( commandService ).clearCart( "user-123" );

      ResponseEntity< Void > response = cartCommandRestService.clearCart();

      assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.NO_CONTENT );
      verify( commandService ).clearCart( "user-123" );
   }

   // ── Helpers ───────────────────────────────────────────────────────────────

   private AddCartItemRequest buildAddItemRequest( Long productId, String name, int quantity ) {
      AddCartItemRequest req = new AddCartItemRequest();
      req.setProductId( productId );
      req.setProductName( name );
      req.setPrice( new BigDecimal( "9.99" ) );
      req.setCurrency( "USD" );
      req.setQuantity( quantity );
      return req;
   }

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
}
