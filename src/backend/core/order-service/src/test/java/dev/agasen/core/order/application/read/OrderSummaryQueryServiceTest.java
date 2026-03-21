package dev.agasen.core.order.application.read;

import dev.agasen.api.order.OrderDetails;
import dev.agasen.api.order.OrderItemDetails;
import dev.agasen.api.order.OrderSummary;
import dev.agasen.api.order.OrderSummaryItem;
import dev.agasen.api.payment.PaymentApi;
import dev.agasen.api.payment.PaymentDetails;
import dev.agasen.api.product.ProductApi;
import dev.agasen.api.product.product.ProductDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class OrderSummaryQueryServiceTest {

   @Mock OrderQueryService orderQueryService;
   @Mock ProductApi productClient;
   @Mock PaymentApi paymentClient;

   @InjectMocks OrderSummaryQueryService service;

   @Test
   void getOrderSummary_returnsCorrectlyAssembledSummary() {
      var item1 = orderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var item2 = orderItem( 2L, "XPS 15", "Dell", new BigDecimal( "1500.00" ), 2 );
      var order = orderDetails( 10L, List.of( item1, item2 ) );

      var product1 = product( 1L, "Samsung Galaxy S24 - 256GB AMOLED display" );
      var product2 = product( 2L, "Dell XPS 15 - 12th Gen Intel, 32GB RAM" );
      var payment = payment( 10L, new BigDecimal( "3999.00" ) );

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
      var item1 = orderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var item2 = orderItem( 2L, "XPS 15", "Dell", new BigDecimal( "1500.00" ), 1 );
      var order = orderDetails( 10L, List.of( item1, item2 ) );

      // product-service only returns product1 — product2 is missing (e.g. deleted)
      var product1 = product( 1L, "Samsung Galaxy S24 - 256GB AMOLED display" );
      var payment = payment( 10L, new BigDecimal( "2499.00" ) );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) ).thenReturn( List.of( product1 ) );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenReturn( payment );

      OrderSummary summary = service.getOrderSummary( 10L );

      assertThat( summary.items().get( 0 ).description() ).isEqualTo( "Samsung Galaxy S24 - 256GB AMOLED display" );
      assertThat( summary.items().get( 1 ).description() ).isNull();
   }

   @Test
   void getOrderSummary_whenPaymentIsNull_summaryStillReturnsWithNullPayment() {
      var item1 = orderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = orderDetails( 10L, List.of( item1 ) );
      var product1 = product( 1L, "Samsung Galaxy S24" );

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
      var item1 = orderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = orderDetails( 10L, List.of( item1 ) );
      var product1 = product( 1L, "Samsung Galaxy S24" );
      var payment = payment( 10L, new BigDecimal( "999.00" ) );

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
      var item1 = orderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = orderDetails( 10L, List.of( item1 ) );
      var product1 = product( 1L, "Samsung Galaxy S24" );

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
      var item1 = orderItem( 1L, "Galaxy S24", "Samsung", new BigDecimal( "999.00" ), 1 );
      var order = orderDetails( 10L, List.of( item1 ) );
      var payment = payment( 10L, new BigDecimal( "999.00" ) );

      when( orderQueryService.getOrderById( 10L ) ).thenReturn( order );
      when( productClient.getProducts( anyList() ) )
         .thenThrow( new RuntimeException( "Product service unavailable" ) );
      when( paymentClient.getPaymentByOrderId( 10L ) ).thenReturn( payment );

      assertThatThrownBy( () -> service.getOrderSummary( 10L ) )
         .isInstanceOf( StructuredTaskScope.FailedException.class )
         .cause()
         .hasMessage( "Product service unavailable" );
   }

   // ── Builders ──────────────────────────────────────────────────────────────

   private static OrderDetails orderDetails( Long id, List< OrderItemDetails > items ) {
      var order = new OrderDetails();
      order.setId( id );
      order.setUserId( "user-1" );
      order.setStatus( "PENDING" );
      order.setTotal( items.stream().map( OrderItemDetails::getLineTotal ).reduce( BigDecimal.ZERO, BigDecimal::add ) );
      order.setItemCount( items.stream().mapToInt( OrderItemDetails::getQuantity ).sum() );
      order.setItems( items );
      order.setCreatedAt( Instant.now() );
      return order;
   }

   private static OrderItemDetails orderItem( Long productId, String name, String brand, BigDecimal price, int qty ) {
      var item = new OrderItemDetails();
      item.setProductId( productId );
      item.setProductName( name );
      item.setBrand( brand );
      item.setPrice( price );
      item.setCurrency( "USD" );
      item.setQuantity( qty );
      item.setLineTotal( price.multiply( BigDecimal.valueOf( qty ) ) );
      return item;
   }

   private static ProductDetails product( Long id, String description ) {
      var product = new ProductDetails();
      product.setId( id );
      product.setDescription( description );
      return product;
   }

   private static PaymentDetails payment( Long orderId, BigDecimal amount ) {
      var payment = new PaymentDetails();
      payment.setId( 100L );
      payment.setOrderId( orderId );
      payment.setAmount( amount );
      payment.setCurrency( "USD" );
      payment.setStatus( "CAPTURED" );
      return payment;
   }
}
