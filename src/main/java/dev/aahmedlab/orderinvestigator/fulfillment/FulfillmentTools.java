package dev.aahmedlab.orderinvestigator.fulfillment;

import java.util.Optional;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentTools {
  private final FulfillmentLookupService fulfillmentLookupService;

  public FulfillmentTools(FulfillmentLookupService fulfillmentLookupService) {
    this.fulfillmentLookupService = fulfillmentLookupService;
  }

  @Tool(
      description =
          "Retrieve recorded fulfillment status and hold reason using an order ID. "
              + "Returns NO_FULFILLMENT_FOUND if absent; this does not establish that the order itself is missing.")
  public String lookupFulfillment(final int orderId) {
    Optional<FulfillmentDetails> fulfillmentDetailsOpt =
        fulfillmentLookupService.findByOrderId(orderId);

    if (fulfillmentDetailsOpt.isEmpty()) {
      return "Order ID: " + orderId + "\n" + "Lookup result: NO_FULFILLMENT_FOUND";
    }

    FulfillmentDetails fulfillmentDetails = fulfillmentDetailsOpt.get();

    return "Order ID: "
        + fulfillmentDetails.orderId()
        + "\n"
        + "Fulfillment status: "
        + fulfillmentDetails.fulfillmentStatus()
        + "\n"
        + "Hold Reason: "
        + fulfillmentDetails.holdReason();
  }
}
