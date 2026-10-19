package dev.aahmedlab.orderinvestigator.fulfillment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentLookupService {
  private final Map<Integer, FulfillmentDetails> fulfillmentById = new HashMap<>();

  public FulfillmentLookupService() {
    fulfillmentById.put(129, new FulfillmentDetails(129, "HELD", "OUT_OF_STOCK"));
  }

  public Optional<FulfillmentDetails> findByOrderId(final int orderId) {
    if (orderId == 130)
      throw new FulfillmentLookupUnavailableException("Fulfillment system is currently down!");

    return Optional.ofNullable(fulfillmentById.get(orderId));
  }
}
