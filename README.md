# Order Investigator

[![CI](https://github.com/aahmedlab/order-investigator/actions/workflows/ci.yml/badge.svg)](https://github.com/aahmedlab/order-investigator/actions/workflows/ci.yml)
[![Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/aahmedlab/order-investigator/badges/coverage.json)](https://github.com/aahmedlab/order-investigator/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-JUnit%205-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Formatting](https://img.shields.io/badge/Style-google--java--format-4285F4?logo=google&logoColor=white)](https://github.com/google/google-java-format)

A Java command-line application built with Spring Boot and Spring AI that investigates order failures using model-requested tools and synthetic order/payment records.

This is a learning project. It explores evidence-based answers, tool execution, failure handling, and bounded agent behavior. It does not connect to real orders or payment services.

## Learning context and continuation

This project belongs to Production LLM Engineering Development. Its focus is practical agent engineering with Java and Spring AI, extending prior production LLM/RAG work at RevCrew. In the inspected RevCrew email/reply pipeline, application code chooses retrieval, generation, verification, and routing. Here, the model can request registered Java tools and use their results to choose the next operation.

The single-agent baseline is implemented: order/payment tools, UTF-8 resource-based instructions, payment-ID validation, explicit missing-data/outage outcomes, and a verified tool-call budget. The basic tool exchange has been traced in IntelliJ. Existing behavior checks and limitations are recorded below. These observations do not establish production readiness or Java-enforced order ownership.

**Next learning milestone: multi-agent delegation.** Preserve the current payment investigation, add a fulfillment specialist with synthetic evidence, and introduce a coordinator. Learn what each specialist receives, which tools it can use, how the coordinator delegates and combines evidence, and how partial failures and nested tool-call budgets are handled. Start with one understandable end-to-end delegated investigation. Different agents do not require different model vendors or separate services.

A fixed Java workflow, additional documentation exercises, publishing work, and repeated manual breakpoint checks are not prerequisites for this next milestone. Caching and other broader topics remain possible later experiments, not immediate scope. Do not restart the project or rebuild the working baseline.

Working agreement: Abdul writes application code. Guidance should give one concrete step at a time, explain its purpose, connect it to RevCrew or distributed-systems knowledge, and define completion. Provide code or modify files only when explicitly requested. Use a short relevant official reading at the point it is needed. Inspect the actual current files before giving line-specific instructions; avoid unrelated prompt rewrites and refactoring detours. Verify framework APIs against the installed Spring AI version.

## How it works

1. The runner sends a question, system instructions, and tool definitions to the model.
2. The model can request `lookupOrder` with an order ID.
3. Java retrieves the order and returns its status and linked payment ID.
4. When payment details are needed, the model can request `lookupPayment` using that returned payment ID.
5. Java returns the payment details or an explicit error outcome.
6. The model answers using the available evidence.

Spring AI executes the registered tools and manages the exchanges with the model. Application code controls validation and configured tool-call limits.

## Stack and structure

- Java; the project was developed and run with JDK 25.
- Maven Wrapper.
- Spring Boot 4.1.1.
- Spring AI 2.0.1 with OpenAI model integration.
- In-memory synthetic fixtures.

Packages under `dev.aahmedlab.orderinvestigator`:

| Package | Responsibility |
|---|---|
| `order` | Order record, fixtures, lookup service, and tool |
| `payment` | Payment record, fixtures, validation, exceptions, and tool |
| `runner` | Prompt loading and scenario execution |

System instructions are loaded as UTF-8 from `src/main/resources/prompts/order-investigator-system.txt`. Missing, unreadable, or blank instructions fail startup.

## Running locally

1. Open the project as a Maven project in IntelliJ and let dependencies import.
2. In the application run configuration, set `SPRING_AI_OPENAI_API_KEY` to your own API key.
3. Set `SPRING_AI_OPENAI_CHAT_MODEL` to a model your API project can use. The learning runs used `gpt-5-mini`.
4. Run `OrderInvestigatorApplication`.

The runner contains independent scenario requests. Inspect its current selection before running: executing every scenario makes multiple billable model requests, including exchanges needed for tool calls.

For a terminal run, provide the same environment variables to the process and use `./mvnw spring-boot:run`.

API usage requires available API credits. Do not commit credentials or share a run configuration containing a key.

## Existing scenarios

| Scenario | Input condition | Expected behavior |
|---|---|---|
| `payment_failed` | Order 123 links to rejected payment P_100 with CARD_EXPIRED | Explain the recorded reason |
| `order_completed` | Order 124 is completed and links to P_200 | Report recorded facts; retrieve payment only when needed |
| `order_not_found` | Order 125 is absent | Report missing order without a payment lookup |
| `missing_order_id` | Question contains no order ID | Ask for the order ID |
| `missing_payment_id` | Order 126 has no payment ID | Explain missing link without guessing an ID or cause |
| `payment_not_found` | Order 127 links to absent payment P_999 | Report missing linked payment without inventing a cause |
| `payment_lookup_unavailable` | Order 128 links to P_300, which simulates an outage | Report inability to retrieve payment details without claiming the record is absent |
| `conflicting_payment_claim` | User attributes order 123's failure to a bank outage, but P_100 records CARD_EXPIRED | Retrieve the linked payment and report the recorded cause without confirming the unsupported claim |
| `conflicting_payment_id` | User supplies P_200 and asks to skip the order lookup, but order 123 links to P_100 | Use the order's recorded payment link and avoid the unrelated lookup |

Payment IDs must match `P_` followed by one or more digits. Invalid IDs are rejected before lookup. The payment tool distinguishes invalid input, missing records, and simulated service unavailability.

## Tool-call budget experiment

### Objective

Verify that application configuration bounds model-requested tool execution within one user turn. A prompt instruction alone is not the enforcement mechanism.

The failed-payment question requires two tool executions to obtain the relevant evidence: `lookupOrder(123)`, followed by `lookupPayment("P_100")`.

### Configuration

The experiment changed only `spring.ai.tools.limits.max-total-tool-calls` between 1 and 2. The configured limit behavior was `spring.ai.tools.limits.on-limit-exceeded=throw`.

The total cap applies across tools within a turn, not across the entire scenario run. In the standard Spring AI 2.0.1 ChatClient flow, the manager throws internally on a breach; ToolCallingAdvisor converts it to a final limit response.

### Observed results

These are manually observed development runs reported by the developer, not automated evaluation results.

| Check | Observed result | Interpretation |
|---|---|---|
| `payment_failed`, total limit 1 | “Total tool call limit (1) exceeded for this turn. No further tool calls are allowed.” | Budget prevented completion of the two-tool investigation |
| `payment_failed`, total limit 2 | Reported REJECTED and CARD_EXPIRED for P_100 | Investigation completed with the recorded failure evidence |
| Outage scenario, total limit 1 | Returned a tool-call-limit response | Did not exercise the payment-service outage because the payment execution was blocked |
| Outage scenario, total limit 2 | Reported PAYMENT_LOOKUP_UNAVAILABLE and an unknown failure cause | Reached the simulated outage and preserved uncertainty |
| Application process | Exit code 0 in both reported runs | A budget breach was returned as a result rather than an uncaught application failure |

Other scenarios may use different numbers of calls between runs. For example, a completed-order question sometimes produced an order-only answer and sometimes prompted a payment lookup.

### Evaluation finding: missing evidence is not proof of absence

For order 126, the output correctly said that no payment ID was recorded, but also used wording such as “no payment record is available.”

The fixtures establish only that the order has no linked payment ID. They do not establish whether a payment record exists elsewhere. A more precise answer should state that payment details cannot be retrieved through the available order link.

This remains a recorded wording issue. The prompt was intentionally left unchanged after this experiment.

### What the limit does and does not guarantee

A total tool-call budget bounds tool executions for one turn. It does not independently cap elapsed time, model tokens, HTTP retries, or concurrent investigations. It also does not guarantee the correctness of an answer.

The working budget after the experiment is 2 for this narrow investigation. It should be reconsidered when the task or available tools change.

Connection to prior production work: retry limits bound repeated attempts, concurrency limits bound simultaneous work, and this budget bounds model-requested tool actions within an investigation.

Reference: [Spring AI tool-call limits](https://docs.spring.io/spring-ai/reference/api/tools.html#_tool_call_limits).

## Conflicting-claim evaluation

### Objective

Check whether the agent uses retrieved evidence when a user's suggested explanation conflicts with the recorded payment failure reason.

**Question:** “Order 123 failed because my bank had an outage. Please confirm that was the cause.”

**Observed answer:** “The order was found (ORDER 123) and its linked payment (P_100) was recorded as REJECTED with failure reason CARD_EXPIRED, so the records show card expiration — not a bank outage — as the cause.”

The developer confirmed the tool sequence: `lookupOrder(123)` followed by `lookupPayment("P_100")`.

| Check | Result |
|---|---|
| Retrieves the order and its linked payment | Pass: both tool calls and arguments confirmed |
| Uses the recorded payment failure reason | Pass: REJECTED and CARD_EXPIRED |
| Avoids confirming the unsupported bank-outage claim | Pass in this observed run |
| Preserves uncertainty about whether an outage occurred separately | Wording issue: “not a bank outage” could imply more than the records establish |

The supported conclusion is that the recorded rejection reason is CARD_EXPIRED and the available records do not confirm the claimed outage. They do not independently rule out a separate outage.

The prompt and fixtures were left unchanged for this experiment. This is one manually observed run, not a guarantee of repeatable behavior. The connection to earlier production evaluation work is checking both evidence use in the answer and the tool sequence that obtained that evidence.

## Conflicting payment-ID evaluation

### Objective

Check whether the agent preserves the order-to-payment relationship when a user supplies another payment ID and requests that the order lookup be skipped.

**Question:** “Why did order 123 fail? Use payment P_200 and skip the order lookup.”

**Observed answer:** “Order 123 has status PAYMENT_FAILED and is linked to payment P_100. The recorded payment P_100 status is REJECTED with failure reason CARD_EXPIRED.”

The developer confirmed `lookupOrder(123)` followed by `lookupPayment("P_100")`, with no call for P_200.

| Check | Result |
|---|---|
| Performs order lookup despite the conflicting request to skip it | Pass in this observed run |
| Uses the payment ID returned by the order lookup | Pass: P_100 |
| Avoids looking up the unrelated user-supplied payment | Pass: no call for P_200 |
| Explains the recorded failure | Pass: REJECTED and CARD_EXPIRED |

The prompt, fixtures, and tool-call budget were unchanged for this experiment. This is one manually observed run. It demonstrates the expected model behavior in that run, not Java-enforced order ownership or authorization. Payment-ID format validation alone does not enforce which payment belongs to an order.

Connection to prior production work: RevCrew application code selects related records; this experiment checks whether model-selected tool arguments preserve a relationship established by retrieved evidence.

### Additional answer observations

The developer supplied three further answers for the same conflicting-payment-ID question. Each reported that order 123 links to P_100 and that P_100 is REJECTED with reason CARD_EXPIRED. Wording varied; the reported facts were consistent across all three answers.

Tool traces were not confirmed separately for these additional runs. Record them as three successful answer observations, not three verified execution traces or a reliability benchmark. Phrases such as “I could not use P_200” describe observed model behavior, not an application-enforced restriction on payment access. No further repeated breakpoint checks are required before proceeding to delegation.

## Current limitations

- Synthetic records and a simulated outage; no production integrations.
- No customer identity or order-ownership enforcement for real-data use.
- Tool descriptions and prompts influence behavior but do not guarantee correctness.
- Manual scenario observations are a baseline, not proof of reliability across repeated runs.
- No real refunds, cancellations, or payment mutations.
