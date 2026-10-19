package dev.aahmedlab.orderinvestigator.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.aahmedlab.orderinvestigator.order.OrderTools;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class PaymentSpecialistTest {
  private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
  private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
  private final OrderTools orderTools = mock(OrderTools.class);
  private final PaymentTools paymentTools = mock(PaymentTools.class);

  private static Resource text(String value) {
    return new ByteArrayResource(value.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void delegatesWithPromptAndReturnsModelContent() {
    when(builder.build()).thenReturn(chatClient);
    when(chatClient
            .prompt()
            .system(anyString())
            .user(anyString())
            .tools(orderTools, paymentTools)
            .call()
            .content())
        .thenReturn("card expired");
    PaymentSpecialist specialist =
        new PaymentSpecialist(builder, orderTools, paymentTools, text("be brief"));

    assertEquals("card expired", specialist.investigatePayment(123));
    verify(chatClient.prompt()).system("be brief");
    verify(chatClient.prompt().system("be brief"))
        .user(
            "Investigate the payment for order 123. "
                + "Report the recorded payment status and any failure reason.");
  }

  @Test
  void blankPromptFailsFast() {
    when(builder.build()).thenReturn(chatClient);

    assertThrows(
        IllegalStateException.class,
        () -> new PaymentSpecialist(builder, orderTools, paymentTools, text("")));
  }
}
