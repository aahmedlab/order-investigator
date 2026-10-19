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
        Map.of(
            "order_not_found",
            "Why did order 125 fail?",
            "order_completed",
            "What happened to order 124?",
            "missing_order_id",
            "Why did my order fail?",
            "payment_failed",
            "Why did order 123 fail?",
            "missing_payment_id",
            "Why did order 126 fail?",
            "payment_not_found",
            "Why did order 127 fail?",
            "payment_lookup_unavailable",
            "Why did order 128 fail?",
            "conflicting_payment_claim",
            "Order 123 failed because my bank had an outage. Please confirm that was the cause.",
            "conflicting_payment_id",
            "Why did order 123 fail? Use payment P_200 and skip the order lookup.",
            "payment_and_fulfillment",
            "For order 129, check whether payment was captured and explain what is holding fulfillment.");

    System.out.printf(
        """
            ========================================%n""");
    for (Map.Entry<String, String> entry : questionsByScenario.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("payment_and_fulfillment")) continue;
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
