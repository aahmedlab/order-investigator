package dev.aahmedlab.orderinvestigator.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FulfillmentToolsTest {
  private final FulfillmentLookupService lookupService = mock(FulfillmentLookupService.class);
  private final FulfillmentTools tools = new FulfillmentTools(lookupService);

  @Test
  void formatsFoundFulfillment() {
    when(lookupService.findByOrderId(129))
        .thenReturn(Optional.of(new FulfillmentDetails(129, "HELD", "OUT_OF_STOCK")));

    assertEquals(
        "Order ID: 129\nFulfillment status: HELD\nHold Reason: OUT_OF_STOCK",
        tools.lookupFulfillment(129));
  }

  @Test
  void reportsNoFulfillmentFound() {
    when(lookupService.findByOrderId(999)).thenReturn(Optional.empty());

    assertEquals(
        "Order ID: 999\nLookup result: NO_FULFILLMENT_FOUND", tools.lookupFulfillment(999));
  }

  @Test
  void endToEndAgainstRealService() {
    FulfillmentTools real = new FulfillmentTools(new FulfillmentLookupService());

    assertEquals(
        "Order ID: 129\nFulfillment status: HELD\nHold Reason: OUT_OF_STOCK",
        real.lookupFulfillment(129));
  }
}
