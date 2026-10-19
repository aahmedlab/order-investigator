package dev.aahmedlab.orderinvestigator.fulfillment;

public record FulfillmentDetails(int orderId, String fulfillmentStatus, String holdReason) {}
