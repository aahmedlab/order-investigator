package dev.aahmedlab.orderinvestigator.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Test
  void order130SimulatesUnavailableService() {
    FulfillmentLookupUnavailableException e =
        assertThrows(FulfillmentLookupUnavailableException.class, () -> service.findByOrderId(130));

    assertEquals("Fulfillment system is currently down!", e.getMessage());
  }

  @Test
  void exceptionKeepsCause() {
    Throwable cause = new RuntimeException("root");

    assertEquals(cause, new FulfillmentLookupUnavailableException("down", cause).getCause());
  }
}
