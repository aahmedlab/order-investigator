package dev.aahmedlab.orderinvestigator.order;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class OrderLookupService {
  private final Map<Integer, OrderDetails> ordersById = new HashMap<>();

  public OrderLookupService() {
    ordersById.put(123, new OrderDetails(123, "PAYMENT_FAILED", "P_100"));
    ordersById.put(124, new OrderDetails(124, "COMPLETED", "P_200"));
    ordersById.put(126, new OrderDetails(126, "ORDER_FAILED", null));
    ordersById.put(127, new OrderDetails(127, "PAYMENT_FAILED", "P_999"));
    ordersById.put(128, new OrderDetails(128, "PAYMENT_FAILED", "P_300"));
    ordersById.put(129, new OrderDetails(129, "PROCESSING", "P_400"));
    ordersById.put(130, new OrderDetails(130, "PROCESSING", "P_500"));
  }

  public Optional<OrderDetails> findByOrderId(final int orderId) {
    return Optional.ofNullable(ordersById.get(orderId));
  }
}
