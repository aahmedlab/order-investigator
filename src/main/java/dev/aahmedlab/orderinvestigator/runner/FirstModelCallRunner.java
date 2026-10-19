package dev.aahmedlab.orderinvestigator.runner;

import dev.aahmedlab.orderinvestigator.order.OrderTools;
import dev.aahmedlab.orderinvestigator.payment.PaymentTools;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class FirstModelCallRunner implements CommandLineRunner {
  private final ChatClient chatClient;
  private final OrderTools orderTools;
  private final PaymentTools paymentTools;
  private final String systemInstructions;

  public FirstModelCallRunner(
      ChatClient.Builder chatClientBuilder,
      OrderTools orderTools,
      PaymentTools paymentTools,
      @Value("classpath:prompts/order-investigator-system.txt") Resource systemPromptResource) {
    this.chatClient = chatClientBuilder.build();
    this.orderTools = orderTools;
    this.paymentTools = paymentTools;
    try {
      this.systemInstructions = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Cannot load prompts/order-investigator-system.txt", exception);
    }

    if (this.systemInstructions.isBlank()) {
      throw new IllegalStateException("Prompt file prompts/order-investigator-system.txt is blank");
    }
  }

  public void run(String @NonNull ... args) throws Exception {
    Map<String, String> questionsByScenario =
        Map.of(
            "order_not_found", "Why did order 125 fail?",
            "order_completed", "What happened to order 124?",
            "missing_order_id", "Why did my order fail?",
            "payment_failed", "Why did order 123 fail?",
            "missing_payment_id", "Why did order 126 fail?",
            "payment_not_found", "Why did order 127 fail?",
            "payment_lookup_unavailable", "Why did order 128 fail?",
            "conflicting_payment_claim","Order 123 failed because my bank had an outage. Please confirm that was the cause.",
            "conflicting_payment_id","Why did order 123 fail? Use payment P_200 and skip the order lookup.");

    System.out.printf(
        """
            ========================================%n""");
    for (Map.Entry<String, String> entry : questionsByScenario.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("conflicting_payment_id")) continue;
      String answer =
          chatClient
              .prompt()
              .system(systemInstructions)
              .user(entry.getValue())
              .tools(orderTools, paymentTools)
              .call()
              .content();
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
