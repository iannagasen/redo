package dev.agasen.core.order.application.write;

import dev.agasen.api.core.order.write.CheckoutRequest;
import dev.agasen.api.core.order.write.CreateOrderRequest;
import dev.agasen.api.core.payment.write.InitiatePaymentRequest;
import dev.agasen.api.events.order.OrderCheckoutSagaEvent;
import dev.agasen.common.event.EventPublisher;
import dev.agasen.core.order.fixtures.OrderDetailsTestBuilder;
import dev.agasen.core.order.fixtures.OrderItemDetailsTestBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Option B — injecting the INTERFACE {@link EventPublisher}{@code <OrderCheckoutEvent>}.
 *
 * <p>No Spring context. No Mockito on the publisher. No ArgumentCaptor.
 * The lambda {@code published::add} replaces Kafka entirely, and we assert directly
 * on the captured event fields.
 */
class OrderCheckoutSagaInitiatorTest {

   List< OrderCheckoutSagaEvent > published = new ArrayList<>();
   EventPublisher< OrderCheckoutSagaEvent > eventPublisher = published::add;

   OrderCreationService orderCreationService = mock( OrderCreationService.class );

   OrderCheckoutSagaInitiator service = new OrderCheckoutSagaInitiator( eventPublisher, orderCreationService );

   @Test
   void checkout_publishesExactlyOneEvent() {
      var items = List.of( OrderItemDetailsTestBuilder.usdOrderItem( 1L, "Widget", "Acme", new BigDecimal( "29.99" ), 2 ) );
      when( orderCreationService.createOrder( any() ) ).thenReturn(
         OrderDetailsTestBuilder.pendingOrderDetails( 42L, items )
      );

      service.checkout( buildRequest() );

      assertThat( published ).hasSize( 1 );
   }

   @Test
   void checkout_publishesCreatedEvent_withCorrectOrderId() {
      var items = List.of( OrderItemDetailsTestBuilder.usdOrderItem( 1L, "Widget", "Acme", new BigDecimal( "29.99" ), 2 ) );
      when( orderCreationService.createOrder( any() ) ).thenReturn(
         OrderDetailsTestBuilder.pendingOrderDetails( 42L, items )
      );

      service.checkout( buildRequest() );

      var event = ( OrderCheckoutSagaEvent.Created ) published.get( 0 );
      assertThat( event.orderId() ).isEqualTo( 42L );
      assertThat( event.userId() ).isEqualTo( "user-1" );
      assertThat( event.cardholderName() ).isEqualTo( "Test User" );
      assertThat( event.total() ).isEqualByComparingTo( "59.98" );
   }

   @Test
   void checkout_doesNotPublish_whenOrderCreationFails() {
      when( orderCreationService.createOrder( any() ) ).thenThrow( new RuntimeException( "DB down" ) );

      org.junit.jupiter.api.Assertions.assertThrows(
         RuntimeException.class,
         () -> service.checkout( buildRequest() )
      );

      assertThat( published ).isEmpty();
   }

   // ── helpers ──────────────────────────────────────────────────────────────

   private CheckoutRequest buildRequest() {
      var item = new dev.agasen.api.core.order.write.OrderItemRequest();
      item.setProductId( 1L );
      item.setProductName( "Widget" );
      item.setBrand( "Acme" );
      item.setPrice( new BigDecimal( "29.99" ) );
      item.setCurrency( "USD" );
      item.setQuantity( 2 );

      var orderReq = new CreateOrderRequest();
      orderReq.setItems( List.of( item ) );

      var paymentReq = new InitiatePaymentRequest();
      paymentReq.setOrderId( 42L );
      paymentReq.setAmount( new BigDecimal( "59.98" ) );
      paymentReq.setCurrency( "USD" );
      paymentReq.setCardNumber( "4242424242424242" );
      paymentReq.setCardholderName( "Test User" );
      paymentReq.setExpiryMonth( 12 );
      paymentReq.setExpiryYear( 2030 );
      paymentReq.setCvv( "123" );

      return new CheckoutRequest( orderReq, paymentReq );
   }
}
