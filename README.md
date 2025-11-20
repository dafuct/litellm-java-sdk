# LiteLLM Java SDK

A robust Java SDK for routing requests across multiple Large Language Model (LLM) providers including OpenAI, Anthropic, and Cohere.

## Features

- **Multi-Provider Routing**: Seamlessly route requests across OpenAI, Anthropic, and Cohere
- **Load Balancing**: Round-robin, weighted, latency-based, and cost-optimized routing strategies
- **Failover**: Automatic failover to secondary providers on primary failure
- **Caching**: Response caching to reduce costs and improve performance
- **Async Support**: Full reactive programming support with Project Reactor
- **Type-Safe**: Strongly-typed Java interfaces with compile-time validation
- **Error Handling**: Comprehensive error handling with retry mechanisms

## Tech Stack

- **Java 17**: Modern Java features including records and pattern matching
- **Spring Boot 3.x**: Reactive web stack with WebFlux
- **Project Reactor**: Reactive programming with Mono/Flux
- **Jackson**: JSON serialization/deserialization
- **Lombok**: Boilerplate code reduction
- **Gradle**: Build automation
- **JUnit 5, Mockito, AssertJ**: Testing framework

## Quick Start

### Prerequisites

- Java 17 or higher
- Gradle 9.x (or use the included wrapper)

### Installation

```bash
git clone https://github.com/your-org/litellm-java-sdk.git
cd litellm-java-sdk
./gradlew build
```

### Basic Usage

```java
import com.litellm.sdk.client.LiteLLMClient;
import com.litellm.sdk.config.ClientConfig;
import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.Message;

// Configure providers
ProviderConfig openAI = ProviderConfig.builder()
    .id("openai-primary")
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .model("gpt-3.5-turbo")
    .build();

// Create client
LiteLLMClient client = ClientConfig.builder()
    .withProvider(openAI)
    .withRoundRobinRouting()
    .build();

// Make request
ChatCompletionRequest request = ChatCompletionRequest.builder()
    .messages(List.of(
        Message.builder()
            .role(Message.Role.USER)
            .content("Hello, world!")
            .build()
    ))
    .model("gpt-3.5-turbo")
    .build();

ChatCompletionResponse response = client.chatCompletion(request);
System.out.println(response.getContent());
```

### Async Usage Example

```java
import com.litellm.sdk.client.AsyncLiteLLMClient;
import reactor.core.publisher.Mono;

public class AsyncExample {
    public static void main(String[] args) {
        AsyncLiteLLMClient client = new AsyncLiteLLMClient(config);

        // Non-blocking async call
        Mono<ChatCompletionResponse> response = client.chatCompletion(request);

        response.subscribe(
            resp -> System.out.println(resp.content()),
            error -> System.err.println("Error: " + error.getMessage())
        );
    }
}
```

## Documentation

- [API Documentation](docs/api.md)
- [Quick Start Guide](specs/001-java-litellm-sdk/quickstart.md)
- [Data Model](specs/001-java-litellm-sdk/data-model.md)
- [API Contracts](specs/001-java-litellm-sdk/contracts/)

## Project Structure

```
src/main/java/com/litellm/sdk/
├── config/              # Configuration models
├── model/               # Request/response models
│   ├── request/         # Request objects
│   ├── response/        # Response objects
│   └── common/          # Shared models
├── provider/            # Provider implementations
│   ├── openai/          # OpenAI provider
│   ├── anthropic/       # Anthropic provider
│   └── cohere/          # Cohere provider
├── routing/             # Routing logic
│   ├── strategy/        # Routing strategies
│   └── failover/        # Failover management
├── cache/               # Caching layer
├── retry/               # Retry mechanisms
├── error/               # Exception hierarchy
└── client/              # Client interfaces
```

## Building

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Generate JaCoCo coverage report
./gradlew test jacocoTestReport
```

## Testing

The project uses a comprehensive testing strategy:

- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end testing with real providers
- **Contract Tests**: API contract validation

```bash
# Run all tests
./gradlew test

# Run unit tests only
./gradlew test --tests "src/test/java/com/litellm/sdk/unit/**"

# Run integration tests
./gradlew test --tests "src/test/java/com/litellm/sdk/integration/**"
```

## Configuration

### Provider Configuration

```java
ProviderConfig config = ProviderConfig.builder()
    .id("provider-id")
    .name("provider-name")
    .apiKey("your-api-key")
    .baseUrl("https://api.provider.com/v1")
    .models(List.of("model1", "model2"))
    .weight(1)
    .timeout(Duration.ofSeconds(30))
    .enabled(true)
    .priority(1)
    .build();
```

### Routing Strategies

- **Round Robin**: Distribute requests evenly
- **Weighted**: Route based on provider weights
- **Latency-Based**: Prefer faster providers
- **Cost-Optimized**: Route to most cost-effective provider
- **Failover**: Primary → Secondary → Tertiary

## Performance

- **Concurrent Requests**: 100+ async requests per client instance
- **Memory Footprint**: <50MB for typical usage
- **Response Time**: <100ms routing overhead p95
- **Availability**: 99.9% through failover

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Ensure all tests pass
6. Submit a pull request

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## Support

- GitHub Issues: [Report a bug](https://github.com/your-org/litellm-java-sdk/issues)
- Documentation: [docs.litellm.ai](https://docs.litellm.ai)

## Acknowledgments

- [LiteLLM](https://github.com/BerriAI/litellm) - The original Python SDK
- [Spring Framework](https://spring.io/projects/spring-framework)
- [Project Reactor](https://projectreactor.io)
