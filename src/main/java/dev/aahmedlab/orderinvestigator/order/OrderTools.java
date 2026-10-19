package dev.aahmedlab.orderinvestigator.order;

import java.util.Optional;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {
  private final OrderLookupService orderLookupService;

  public OrderTools(OrderLookupService orderLookupService) {
    this.orderLookupService = orderLookupService;
  }

  @Tool(
      description =
          "Retrieve an order's recorded status and linked payment ID using its order ID. "
              + "Returns NO_ORDER_FOUND if absent.")
  public String lookupOrder(final int orderId) {
    Optional<OrderDetails> orderDetailsOpt = orderLookupService.findByOrderId(orderId);

    if (orderDetailsOpt.isEmpty()) {
      return "Order ID: " + orderId + "\n" + "Lookup result: NO_ORDER_FOUND";
    }

    OrderDetails order = orderDetailsOpt.get();

    return "Order ID: "
        + order.orderId()
        + "\n"
        + "Order status: "
        + order.orderStatus()
        + "\n"
        + "Payment ID: "
        + order.paymentId();
  }
}
