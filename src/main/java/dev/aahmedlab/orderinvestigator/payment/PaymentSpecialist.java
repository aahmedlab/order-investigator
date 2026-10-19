package dev.aahmedlab.orderinvestigator.payment;

import dev.aahmedlab.orderinvestigator.order.OrderTools;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PaymentSpecialist {
  private final ChatClient chatClient;
  private final OrderTools orderTools;
  private final PaymentTools paymentTools;
  private final String systemInstructions;

  public PaymentSpecialist(
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

  @Tool(
      description =
          "Delegate payment investigation for an order ID. "
              + "Looks up the order and its linked payment, then returns payment findings "
              + "or explains why payment evidence could not be retrieved. "
              + "Does not inspect fulfillment records.")
  public String investigatePayment(int orderId) {
    String userQuestion =
        String.format(
            "Investigate the payment for order %d. Report the recorded payment status and any failure reason.",
            orderId);
    return chatClient
        .prompt()
        .system(systemInstructions)
        .user(userQuestion)
        .tools(orderTools, paymentTools)
        .call()
        .content();
  }
}
