package dev.aahmedlab.orderinvestigator.payment;

import java.util.Optional;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class PaymentTools {
  private final PaymentLookupService paymentLookupService;

  public PaymentTools(PaymentLookupService paymentLookupService) {
    this.paymentLookupService = paymentLookupService;
  }

  @Tool
  public String lookupPayment(final String paymentId) {
    Optional<PaymentDetails> paymentDetailsOpt;
    try {
      paymentDetailsOpt = paymentLookupService.findByPaymentId(paymentId);
    } catch (PaymentLookupUnavailableException e) {
      return "Payment ID: "
              + paymentId + "\n"
              + "error  : PAYMENT_LOOKUP_UNAVAILABLE" + "\n"
              + "message: the payment service is temporarily unavailable";
    } catch (InvalidPaymentIdException e) {
      return "Payment ID: "
              + paymentId + "\n"
              + "error  : INVALID_PAYMENT_ID" + "\n"
              + "message: " + e.getMessage();
    }

    if (paymentDetailsOpt.isEmpty()) {
      return "Payment ID: " + paymentId + "\n" + "Payment lookup result: NO_PAYMENT_FOUND";
    }

    PaymentDetails paymentDetails = paymentDetailsOpt.get();
    String failureReason = paymentDetails.paymentFailureReason();
    return "Payment ID: "
        + paymentDetails.paymentId()
        + "\n"
        + "Payment status: "
        + paymentDetails.paymentStatus()
        + "\n"
        + "Payment Failure Reason: "
        + ((failureReason == null || failureReason.isBlank())
            ? "No payment failure reason recorded."
            : failureReason);
  }
}
