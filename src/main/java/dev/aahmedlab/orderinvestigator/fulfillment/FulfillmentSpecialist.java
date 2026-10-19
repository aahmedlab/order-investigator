package dev.aahmedlab.orderinvestigator.fulfillment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentSpecialist {
  private final ChatClient chatClient;
  private final FulfillmentTools fulfillmentTools;
  private final String systemInstructions;

  public FulfillmentSpecialist(
      ChatClient.Builder chatClientBuilder,
      FulfillmentTools fulfillmentTools,
      @Value("classpath:prompts/fulfillment-investigator-system.txt")
          Resource systemPromptResource) {
    this.chatClient = chatClientBuilder.build();
    this.fulfillmentTools = fulfillmentTools;
    try {
      this.systemInstructions = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Cannot load prompts/fulfillment-investigator-system.txt", exception);
    }

    if (this.systemInstructions.isBlank()) {
      throw new IllegalStateException(
          "Prompt file prompts/fulfillment-investigator-system.txt is blank");
    }
  }

  @Tool(
      description =
          "Delegate fulfillment investigation for an order ID. "
              + "Returns findings about fulfillment status and any recorded hold reason, "
              + "or reports missing fulfillment evidence. "
              + "Does not inspect payment records.")
  public String investigateFulfillment(int orderId) {
    String userQuestion =
        String.format(
            "Investigate the fulfillment for order %d. Report the recorded fulfillment status and any held reason.",
            orderId);
    return chatClient
        .prompt()
        .system(systemInstructions)
        .user(userQuestion)
        .tools(fulfillmentTools)
        .call()
        .content();
  }
}
