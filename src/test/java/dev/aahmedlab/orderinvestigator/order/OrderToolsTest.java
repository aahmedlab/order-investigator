package dev.aahmedlab.orderinvestigator.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrderToolsTest {
  private final OrderLookupService orderLookupService = mock(OrderLookupService.class);
  private final OrderTools tools = new OrderTools(orderLookupService);

  @Test
  void formatsFoundOrder() {
    when(orderLookupService.findByOrderId(123))
        .thenReturn(Optional.of(new OrderDetails(123, "PAYMENT_FAILED", "P_100")));

    assertEquals(
        "Order ID: 123\nOrder status: PAYMENT_FAILED\nPayment ID: P_100", tools.lookupOrder(123));
    verify(orderLookupService).findByOrderId(123);
  }

  @Test
  void formatsNullPaymentIdAsText() {
    when(orderLookupService.findByOrderId(126))
        .thenReturn(Optional.of(new OrderDetails(126, "ORDER_FAILED", null)));

    assertEquals(
        "Order ID: 126\nOrder status: ORDER_FAILED\nPayment ID: null", tools.lookupOrder(126));
  }

  @Test
  void reportsNoOrderFound() {
    when(orderLookupService.findByOrderId(999)).thenReturn(Optional.empty());

    assertEquals("Order ID: 999\nLookup result: NO_ORDER_FOUND", tools.lookupOrder(999));
  }

  @Test
  void worksAgainstRealService() {
    OrderTools real = new OrderTools(new OrderLookupService());

    assertEquals(
        "Order ID: 124\nOrder status: COMPLETED\nPayment ID: P_200", real.lookupOrder(124));
  }
}
