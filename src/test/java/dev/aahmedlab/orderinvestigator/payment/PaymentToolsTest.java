package dev.aahmedlab.orderinvestigator.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PaymentToolsTest {
  private final PaymentLookupService paymentLookupService = mock(PaymentLookupService.class);
  private final PaymentTools tools = new PaymentTools(paymentLookupService);

  @Test
  void formatsPaymentWithFailureReason() {
    when(paymentLookupService.findByPaymentId("P_100"))
        .thenReturn(Optional.of(new PaymentDetails("P_100", "REJECTED", "CARD_EXPIRED")));

    assertEquals(
        "Payment ID: P_100\nPayment status: REJECTED\nPayment Failure Reason: CARD_EXPIRED",
        tools.lookupPayment("P_100"));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  void blankOrNullFailureReasonShowsPlaceholder(String reason) {
    when(paymentLookupService.findByPaymentId("P_200"))
        .thenReturn(Optional.of(new PaymentDetails("P_200", "CAPTURED", reason)));

    assertEquals(
        "Payment ID: P_200\nPayment status: CAPTURED\n"
            + "Payment Failure Reason: No payment failure reason recorded.",
        tools.lookupPayment("P_200"));
  }

  @Test
  void reportsNoPaymentFound() {
    when(paymentLookupService.findByPaymentId("P_999")).thenReturn(Optional.empty());

    assertEquals(
        "Payment ID: P_999\nPayment lookup result: NO_PAYMENT_FOUND", tools.lookupPayment("P_999"));
  }

  @Test
  void reportsServiceUnavailable() {
    when(paymentLookupService.findByPaymentId("P_300"))
        .thenThrow(new PaymentLookupUnavailableException("down"));

    assertEquals(
        "Payment ID: P_300\nerror  : PAYMENT_LOOKUP_UNAVAILABLE\n"
            + "message: the payment service is temporarily unavailable",
        tools.lookupPayment("P_300"));
  }

  @Test
  void reportsInvalidPaymentId() {
    when(paymentLookupService.findByPaymentId("bad"))
        .thenThrow(new InvalidPaymentIdException("bad id"));

    assertEquals(
        "Payment ID: bad\nerror  : INVALID_PAYMENT_ID\nmessage: bad id",
        tools.lookupPayment("bad"));
  }

  @Test
  void endToEndAgainstRealService() {
    PaymentTools real = new PaymentTools(new PaymentLookupService());

    assertEquals(
        "Payment ID: P_300\nerror  : PAYMENT_LOOKUP_UNAVAILABLE\n"
            + "message: the payment service is temporarily unavailable",
        real.lookupPayment("P_300"));
    assertEquals(
        "Payment ID: null\nerror  : INVALID_PAYMENT_ID\nmessage: "
            + "Payment Id cannot be empty, must start with 'P_', and must be followed by a number",
        real.lookupPayment(null));
  }
}
