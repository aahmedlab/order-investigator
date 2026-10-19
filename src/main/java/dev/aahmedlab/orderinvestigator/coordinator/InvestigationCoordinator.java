package dev.aahmedlab.orderinvestigator.coordinator;

import dev.aahmedlab.orderinvestigator.fulfillment.FulfillmentSpecialist;
import dev.aahmedlab.orderinvestigator.payment.PaymentSpecialist;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class InvestigationCoordinator {
  private final ChatClient chatClient;
  private final PaymentSpecialist paymentSpecialist;
  private final FulfillmentSpecialist fulfillmentSpecialist;
  private final String systemInstructions;

  public InvestigationCoordinator(
      ChatClient.Builder chatClientBuilder,
      PaymentSpecialist paymentSpecialist,
      FulfillmentSpecialist fulfillmentSpecialist,
      @Value("classpath:prompts/coordinator-specific-system.txt") Resource systemPromptResource) {

    this.chatClient = chatClientBuilder.build();
    this.paymentSpecialist = paymentSpecialist;
    this.fulfillmentSpecialist = fulfillmentSpecialist;
    try {
      this.systemInstructions = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Cannot load prompts/coordinator-specific-system.txt", exception);
    }

    if (this.systemInstructions.isBlank()) {
      throw new IllegalStateException(
          "Prompt file prompts/coordinator-specific-system.txt is blank");
    }
  }

  public String investigate(String userQuestion) {

    return chatClient
        .prompt()
        .system(systemInstructions)
        .user(userQuestion)
        .tools(paymentSpecialist, fulfillmentSpecialist)
        .call()
        .content();
  }
}
