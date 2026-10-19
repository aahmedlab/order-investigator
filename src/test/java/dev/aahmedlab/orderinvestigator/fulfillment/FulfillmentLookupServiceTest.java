package dev.aahmedlab.orderinvestigator.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FulfillmentLookupServiceTest {
  private final FulfillmentLookupService service = new FulfillmentLookupService();

  @Test
  void findsHeldFulfillment() {
    assertEquals(
        new FulfillmentDetails(129, "HELD", "OUT_OF_STOCK"),
        service.findByOrderId(129).orElseThrow());
  }

  @Test
  void unknownOrderIdReturnsEmpty() {
    assertTrue(service.findByOrderId(999).isEmpty());
  }
}
