package dev.aahmedlab.orderinvestigator.coordinator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.aahmedlab.orderinvestigator.fulfillment.FulfillmentSpecialist;
import dev.aahmedlab.orderinvestigator.payment.PaymentSpecialist;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class InvestigationCoordinatorTest {
  private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
  private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
  private final PaymentSpecialist payment = mock(PaymentSpecialist.class);
  private final FulfillmentSpecialist fulfillment = mock(FulfillmentSpecialist.class);

  private static Resource text(String value) {
    return new ByteArrayResource(value.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void investigateDelegatesToSpecialistsAndReturnsContent() {
    when(builder.build()).thenReturn(chatClient);
    when(chatClient
            .prompt()
            .system(anyString())
            .user(anyString())
            .tools(payment, fulfillment)
            .call()
            .content())
        .thenReturn("combined findings");
    InvestigationCoordinator coordinator =
        new InvestigationCoordinator(builder, payment, fulfillment, text("coordinate"));

    assertEquals("combined findings", coordinator.investigate("Why did order 129 fail?"));
    verify(chatClient.prompt()).system("coordinate");
    verify(chatClient.prompt().system("coordinate")).user("Why did order 129 fail?");
  }

  @Test
  void blankPromptFailsFast() {
    when(builder.build()).thenReturn(chatClient);

    assertThrows(
        IllegalStateException.class,
        () -> new InvestigationCoordinator(builder, payment, fulfillment, text(" \n")));
  }
}
