package dev.aahmedlab.orderinvestigator.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class FulfillmentSpecialistTest {
  private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
  private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
  private final FulfillmentTools tools = mock(FulfillmentTools.class);

  private static Resource text(String value) {
    return new ByteArrayResource(value.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void delegatesWithPromptAndReturnsModelContent() {
    when(builder.build()).thenReturn(chatClient);
    when(chatClient.prompt().system(anyString()).user(anyString()).tools(tools).call().content())
        .thenReturn("held: out of stock");
    FulfillmentSpecialist specialist = new FulfillmentSpecialist(builder, tools, text("be brief"));

    assertEquals("held: out of stock", specialist.investigateFulfillment(129));
    verify(chatClient.prompt()).system("be brief");
    verify(chatClient.prompt().system("be brief"))
        .user(
            "Investigate the fulfillment for order 129. "
                + "Report the recorded fulfillment status and any held reason.");
  }

  @Test
  void blankPromptFailsFast() {
    when(builder.build()).thenReturn(chatClient);

    assertThrows(
        IllegalStateException.class, () -> new FulfillmentSpecialist(builder, tools, text("  ")));
  }
}
