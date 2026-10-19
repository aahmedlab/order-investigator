package dev.aahmedlab.orderinvestigator.payment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PaymentLookupService {
  private final Map<String, PaymentDetails> paymentById = new HashMap<>();

  public PaymentLookupService() {
    paymentById.put("P_100", new PaymentDetails("P_100", "REJECTED", "CARD_EXPIRED"));
    paymentById.put("P_200", new PaymentDetails("P_200", "CAPTURED", ""));
    paymentById.put("P_400", new PaymentDetails("P_400", "CAPTURED", ""));
  }

  public Optional<PaymentDetails> findByPaymentId(final String paymentId) {
    validatePaymentId(paymentId);
    if (paymentId.equals("P_300"))
      throw new PaymentLookupUnavailableException("Payment system is currently down!");
    return Optional.ofNullable(paymentById.get(paymentId));
  }

  private void validatePaymentId(final String paymentId) {
    if (paymentId == null || !paymentId.matches("P_\\d+")) {
      throw new InvalidPaymentIdException(
          "Payment Id cannot be empty, must start with 'P_', and must be followed by a number");
    }
  }
}
