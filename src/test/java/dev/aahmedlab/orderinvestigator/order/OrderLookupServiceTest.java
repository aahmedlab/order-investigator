package dev.aahmedlab.orderinvestigator.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrderLookupServiceTest {
  private final OrderLookupService service = new OrderLookupService();

  @Test
  void findsOrderWithFailedPayment() {
    Optional<OrderDetails> result = service.findByOrderId(123);

    assertTrue(result.isPresent());
    assertEquals(new OrderDetails(123, "PAYMENT_FAILED", "P_100"), result.get());
  }

  @Test
  void findsCompletedOrder() {
    OrderDetails order = service.findByOrderId(124).orElseThrow();

    assertEquals("COMPLETED", order.orderStatus());
    assertEquals("P_200", order.paymentId());
  }

  @Test
  void orderWithoutPaymentHasNullPaymentId() {
    OrderDetails order = service.findByOrderId(126).orElseThrow();

    assertEquals("ORDER_FAILED", order.orderStatus());
    assertNull(order.paymentId());
  }

  @Test
  void unknownOrderReturnsEmpty() {
    assertTrue(service.findByOrderId(999).isEmpty());
  }

  @Test
  void negativeOrderIdReturnsEmpty() {
    assertTrue(service.findByOrderId(-1).isEmpty());
  }

  @Test
  void findsOrder130LinkedToP500() {
    assertEquals(
        new OrderDetails(130, "PROCESSING", "P_500"), service.findByOrderId(130).orElseThrow());
  }
}
