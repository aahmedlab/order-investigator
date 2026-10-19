package dev.aahmedlab.orderinvestigator.fulfillment;

public class FulfillmentLookupUnavailableException extends RuntimeException {
  public FulfillmentLookupUnavailableException(String message) {
    super(message);
  }

  public FulfillmentLookupUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
