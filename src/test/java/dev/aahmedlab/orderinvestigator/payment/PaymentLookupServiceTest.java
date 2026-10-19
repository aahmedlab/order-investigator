package dev.aahmedlab.orderinvestigator.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PaymentLookupServiceTest {
  private final PaymentLookupService service = new PaymentLookupService();

  @Test
  void findsRejectedPayment() {
    Optional<PaymentDetails> result = service.findByPaymentId("P_100");

    assertEquals(new PaymentDetails("P_100", "REJECTED", "CARD_EXPIRED"), result.orElseThrow());
  }

  @Test
  void findsCapturedPayment() {
    PaymentDetails payment = service.findByPaymentId("P_200").orElseThrow();

    assertEquals("CAPTURED", payment.paymentStatus());
    assertEquals("", payment.paymentFailureReason());
  }

  @Test
  void unknownButWellFormedIdReturnsEmpty() {
    assertTrue(service.findByPaymentId("P_999").isEmpty());
  }

  @Test
  void p300SimulatesUnavailableService() {
    PaymentLookupUnavailableException e =
        assertThrows(
            PaymentLookupUnavailableException.class, () -> service.findByPaymentId("P_300"));

    assertEquals("Payment system is currently down!", e.getMessage());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(
      strings = {
        " ", "100", "p_100", "P_", "P_abc", "P_10x", "P-100", " P_100", "P_100 ", "P_100\n"
      })
  void invalidPaymentIdThrows(String paymentId) {
    assertThrows(InvalidPaymentIdException.class, () -> service.findByPaymentId(paymentId));
  }
}
