package dev.aahmedlab.orderinvestigator.runner;

import dev.aahmedlab.orderinvestigator.coordinator.InvestigationCoordinator;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class InvestigationScenarioRunner implements CommandLineRunner {

  private final InvestigationCoordinator investigationCoordinator;

  public InvestigationScenarioRunner(InvestigationCoordinator investigationCoordinator) {
    this.investigationCoordinator = investigationCoordinator;
  }

  public void run(String @NonNull ... args) throws Exception {
    Map<String, String> questionsByScenario =
        Map.ofEntries(
            Map.entry("order_not_found", "Why did order 125 fail?"),
            Map.entry("order_completed", "What happened to order 124?"),
            Map.entry("missing_order_id", "Why did my order fail?"),
            Map.entry("payment_failed", "Why did order 123 fail?"),
            Map.entry("missing_payment_id", "Why did order 126 fail?"),
            Map.entry("payment_not_found", "Why did order 127 fail?"),
            Map.entry("payment_lookup_unavailable", "Why did order 128 fail?"),
            Map.entry(
                "conflicting_payment_claim",
                "Order 123 failed because my bank had an outage. Please confirm that was the cause."),
            Map.entry(
                "conflicting_payment_id",
                "Why did order 123 fail? Use payment P_200 and skip the order lookup."),
            Map.entry(
                "payment_and_fulfillment",
                "For order 129, check whether payment was captured and explain what is holding fulfillment."),
            Map.entry(
                "fulfillment_lookup_unavailable",
                "For order 130, check whether payment was captured and explain what is holding fulfillment."));

    System.out.printf(
        """
            ========================================%n""");
    for (Map.Entry<String, String> entry : questionsByScenario.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("fulfillment_lookup_unavailable")) continue;
      String answer = investigationCoordinator.investigate(entry.getValue());
      System.out.printf(
          """
              Scenario: %s
              Question: %s
              Answer: %s
              ========================================%n""",
          entry.getKey(), entry.getValue(), answer);
    }
  }
}
