package dev.agasen.core.order.application.read;

import dev.agasen.api.order.read.OrderSummary;
import dev.agasen.api.order.read.OrderSummaryItem;
import dev.agasen.api.payment.PaymentApi;
import dev.agasen.api.product.ProductApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

import static dev.agasen.core.order.fixtures.OrderDetailsTestBuilder.*;
import static dev.agasen.core.order.fixtures.OrderItemDetailsTestBuilder.*;
import static dev.agasen.core.order.fixtures.PaymentDetailsTestBuilder.*;
import static dev.agasen.core.order.fixtures.ProductDetailsTestBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class OrderSummaryAggregatorTest {

   @Mock OrderQueryService orderQueryService;
   @Mock ProductApi productClient;
   @Mock PaymentApi paymentClient;

   @InjectMocks OrderSummaryAggregator service;

   @Test
   void getOrderSummary_returnsCorrectlyAssembledSummary() {
      var item1 = usdOrderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var item2 = usdOrderItem( 2L, "XPS 15", "Dell", new BigDecimal( "1500.00" ), 2 );
      var order = pendingOrderDetails( 10L, List.of( item1, item2 ) );

      var product1 = productDetails( 1L, "Samsung Galaxy S24 - 256GB AMOLED display" );
      var product2 = productDetails( 2L, "Dell XPS 15 - 12th Gen Intel, 32GB RAM" );
      var payment = capturedPayment( 10L, new BigDecimal( "3999.00" ) );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) ).thenReturn( List.of( product1, product2 ) );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenReturn( payment );

      OrderSummary summary = service.getOrderSummary( 10L );

      assertThat( summary.id() ).isEqualTo( 10L );
      assertThat( summary.status() ).isEqualTo( "PENDING" );
      assertThat( summary.payment() ).isEqualTo( payment );
      assertThat( summary.items() )
         .extracting( OrderSummaryItem::description )
         .containsExactlyInAnyOrder(
            "Samsung Galaxy S24 - 256GB AMOLED display",
            "Dell XPS 15 - 12th Gen Intel, 32GB RAM"
         );
   }

   @Test
   void getOrderSummary_whenProductMissingFromBatchResponse_descriptionIsNull() {
      var item1 = usdOrderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var item2 = usdOrderItem( 2L, "XPS 15", "Dell", new BigDecimal( "1500.00" ), 1 );
      var order = pendingOrderDetails( 10L, List.of( item1, item2 ) );

      // product-service only returns product1 — product2 is missing (e.g. deleted)
      var product1 = productDetails( 1L, "Samsung Galaxy S24 - 256GB AMOLED display" );
      var payment = capturedPayment( 10L, new BigDecimal( "2499.00" ) );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) ).thenReturn( List.of( product1 ) );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenReturn( payment );

      OrderSummary summary = service.getOrderSummary( 10L );

      assertThat( summary.items().get( 0 ).description() ).isEqualTo( "Samsung Galaxy S24 - 256GB AMOLED display" );
      assertThat( summary.items().get( 1 ).description() ).isNull();
   }

   @Test
   void getOrderSummary_whenPaymentIsNull_summaryStillReturnsWithNullPayment() {
      var item1 = usdOrderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = pendingOrderDetails( 10L, List.of( item1 ) );
      var product1 = productDetails( 1L, "Samsung Galaxy S24" );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) ).thenReturn( List.of( product1 ) );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenReturn( null );

      OrderSummary summary = service.getOrderSummary( 10L );

      assertThat( summary.payment() ).isNull();
      assertThat( summary.items() ).hasSize( 1 );
   }

   @Test
   @Timeout( 5 )
   void getOrderSummary_tasksRunConcurrently() throws Exception {
      var item1 = usdOrderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = pendingOrderDetails( 10L, List.of( item1 ) );
      var product1 = productDetails( 1L, "Samsung Galaxy S24" );
      var payment = capturedPayment( 10L, new BigDecimal( "999.00" ) );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) ).thenAnswer( inv -> {
         Thread.sleep( 3_000 ); // 3s delay
         return List.of( product1 );
      } );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenAnswer( inv -> {
         Thread.sleep( 3_000 ); // 3s delay
         return payment;
      } );

      long start = System.currentTimeMillis();
      OrderSummary summary = service.getOrderSummary( 10L );
      long elapsed = System.currentTimeMillis() - start;

      // Sequential would take ~5s; parallel should complete in ~3s (the slower task)
      assertThat( elapsed ).isGreaterThanOrEqualTo( 3_000 )
         .isLessThan( 4_500 );
      assertThat( summary ).isNotNull();
   }

   @Test
   void getOrderSummary_whenPaymentTaskThrows_failedExceptionIsPropagated() {
      var item1 = usdOrderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = pendingOrderDetails( 10L, List.of( item1 ) );
      var product1 = productDetails( 1L, "Samsung Galaxy S24" );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) ).thenReturn( List.of( product1 ) );
      when( paymentClient.getPaymentByOrderId( 10L ) )
         .thenThrow( new RuntimeException( "Payment service unavailable" ) );

      assertThatThrownBy( () -> service.getOrderSummary( 10L ) )
         .isInstanceOf( StructuredTaskScope.FailedException.class )
         .cause()
         .hasMessage( "Payment service unavailable" );
   }

   @Test
   void getOrderSummary_whenProductTaskThrows_failedExceptionIsPropagated() {
      var item1 = usdOrderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = pendingOrderDetails( 10L, List.of( item1 ) );
      var payment = capturedPayment( 10L, new BigDecimal( "999.00" ) );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) )
         .thenThrow( new RuntimeException( "Product service unavailable" ) );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenReturn( payment );

      assertThatThrownBy( () -> service.getOrderSummary( 10L ) )
         .isInstanceOf( StructuredTaskScope.FailedException.class )
         .cause()
         .hasMessage( "Product service unavailable" );
   }

}
