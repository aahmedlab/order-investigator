package dev.aahmedlab.orderinvestigator.runner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.aahmedlab.orderinvestigator.coordinator.InvestigationCoordinator;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class InvestigationScenarioRunnerTest {
  private final InvestigationCoordinator coordinator = mock(InvestigationCoordinator.class);

  @Test
  void runsSelectedScenarioAndPrintsAnswer() throws Exception {
    String question =
        "For order 129, check whether payment was captured and explain what is holding fulfillment.";
    when(coordinator.investigate(anyString())).thenReturn("captured; held out of stock");
    PrintStream original = System.out;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    try {
      new InvestigationScenarioRunner(coordinator).run();
    } finally {
      System.setOut(original);
    }

    verify(coordinator).investigate(question);
    String output = captured.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("Scenario: payment_and_fulfillment"));
    assertTrue(output.contains("Answer: captured; held out of stock"));
  }
}
