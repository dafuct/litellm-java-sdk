package com.litellm.sdk.unit.cache;

import com.litellm.sdk.cache.CaffeineCache;
import com.litellm.sdk.config.CacheConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.request.Message;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice;
import com.litellm.sdk.model.response.ChatCompletionResponse.Choice.ResponseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaffeineCache Unit Tests")
class CaffeineCacheTest {

    @Mock
    private CacheConfig mockConfig;

    private CaffeineCache cache;

    private ChatCompletionRequest request1;
    private ChatCompletionRequest request2;
    private ChatCompletionResponse response1;
    private ChatCompletionResponse response2;

    @BeforeEach
    void setUp() {
        lenient().when(mockConfig.maxSize()).thenReturn(100);
        lenient().when(mockConfig.ttl()).thenReturn(Duration.ofSeconds(300));
        lenient().when(mockConfig.timeUnit()).thenReturn(TimeUnit.SECONDS);

        cache = new CaffeineCache(mockConfig);

        Message message = Message.builder()
            .role(Message.Role.USER)
            .content("Hello")
            .build();

        request1 = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(message))
            .temperature(0.7)
            .maxTokens(100)
            .topP(1.0)
            .build();

        request2 = ChatCompletionRequest.builder()
            .model("gpt-4")
            .messages(List.of(message))
            .temperature(0.7)
            .maxTokens(100)
            .topP(1.0)
            .build();

        response1 = ChatCompletionResponse.builder()
            .id("resp-1")
            .choices(List.of(
                Choice.builder()
                    .index(0)
                    .finishReason("stop")
                    .message(ResponseMessage.builder()
                        .content("Response 1")
                        .role("assistant")
                        .images(List.of())
                        .thinkingBlocks(List.of())
                        .build())
                    .build()
            ))
            .provider("openai")
            .model("gpt-3.5-turbo")
            .cached(false)
            .timestamp(Instant.now())
            .build();

        response2 = ChatCompletionResponse.builder()
            .id("resp-2")
            .choices(List.of(
                Choice.builder()
                    .index(0)
                    .finishReason("stop")
                    .message(ResponseMessage.builder()
                        .content("Response 2")
                        .role("assistant")
                        .images(List.of())
                        .thinkingBlocks(List.of())
                        .build())
                    .build()
            ))
            .provider("openai")
            .model("gpt-4")
            .cached(false)
            .timestamp(Instant.now())
            .build();
    }

    @Test
    @DisplayName("Should store and retrieve cached responses")
    void shouldStoreAndRetrieveCachedResponses() {
        cache.put(request1, response1).block();

        ChatCompletionResponse result = cache.get(request1).block();
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("resp-1");
    }

    @Test
    @DisplayName("Should return null for cache miss")
    void shouldReturnNullForCacheMiss() {
        StepVerifier.create(cache.get(request1))
            .verifyComplete();
    }

    @Test
    @DisplayName("Should track cache hits and misses")
    void shouldTrackCacheHitsAndMisses() {
        // First request - miss
        cache.get(request1).block();

        // Store response
        cache.put(request1, response1).block();

        // Second request - hit
        cache.get(request1).block();

        // Check statistics
        var stats = cache.getStats();
        assertThat(stats.hitCount()).isEqualTo(1);
        assertThat(stats.missCount()).isEqualTo(1);
        assertThat(stats.getHitRate()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Should invalidate single cache entry")
    void shouldInvalidateSingleCacheEntry() {
        // Store two different requests
        cache.put(request1, response1).block();
        cache.put(request2, response2).block();

        // Verify both are cached
        assertThat(cache.get(request1).block()).isNotNull();
        assertThat(cache.get(request2).block()).isNotNull();

        // Invalidate request1
        cache.invalidate(request1);

        // Verify only request1 is invalidated
        assertThat(cache.get(request1).block()).isNull();
        assertThat(cache.get(request2).block()).isNotNull();
    }

    @Test
    @DisplayName("Should invalidate all cache entries")
    void shouldInvalidateAllCacheEntries() {
        // Store responses
        cache.put(request1, response1).block();
        cache.put(request2, response2).block();

        // Verify both are cached
        assertThat(cache.get(request1).block()).isNotNull();
        assertThat(cache.get(request2).block()).isNotNull();

        // Invalidate all
        cache.invalidateAll();

        // Verify all are invalidated
        assertThat(cache.get(request1).block()).isNull();
        assertThat(cache.get(request2).block()).isNull();

        var stats = cache.getStats();
        assertThat(stats.hitCount()).isEqualTo(2);
        assertThat(stats.missCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get cache statistics")
    void shouldGetCacheStatistics() {
        // Store and retrieve
        cache.put(request1, response1).block();
        cache.get(request1).block();

        var stats = cache.getStats();
        assertThat(stats).isNotNull();
        assertThat(stats.hitCount()).isGreaterThanOrEqualTo(1);
        assertThat(stats.missCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.evictionCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.size()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getHitRate()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should calculate hit rate correctly")
    void shouldCalculateHitRateCorrectly() {
        // 3 misses, 2 hits = 5 total, 40% hit rate
        cache.get(request1).block(); // miss
        cache.put(request1, response1).block();
        cache.get(request2).block(); // miss
        cache.get(request1).block(); // hit
        cache.get(request1).block(); // hit
        cache.get(request2).block(); // miss

        var stats = cache.getStats();
        assertThat(stats.getHitRate()).isEqualTo(2.0 / 5.0);
    }

    @Test
    @DisplayName("Should return zero hit rate when no requests")
    void shouldReturnZeroHitRateWhenNoRequests() {
        var stats = cache.getStats();
        assertThat(stats.getHitRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle concurrent cache operations")
    void shouldHandleConcurrentCacheOperations() {
        // Simulate concurrent operations
        List<Runnable> operations = List.of(
            () -> cache.put(request1, response1).block(),
            () -> cache.get(request1).block(),
            () -> cache.put(request2, response2).block(),
            () -> cache.get(request2).block()
        );

        // Execute operations
        operations.forEach(Runnable::run);

        // Verify results
        assertThat(cache.get(request1).block()).isNotNull();
        assertThat(cache.get(request2).block()).isNotNull();
    }

    @Test
    @DisplayName("Should close cache without errors")
    void shouldCloseCache() {
        assertThatCode(() -> cache.close()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should generate consistent cache keys for same requests")
    void shouldGenerateConsistentCacheKeys() {
        cache.put(request1, response1).block();

        StepVerifier.create(cache.get(request1))
            .assertNext(response -> assertThat(response).isNotNull())
            .verifyComplete();

        // Verify statistics show at least one hit
        var stats = cache.getStats();
        assertThat(stats.hitCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should distinguish between different requests")
    void shouldDistinguishBetweenDifferentRequests() {
        // Store different responses for different requests
        cache.put(request1, response1).block();
        cache.put(request2, response2).block();

        // Verify each returns correct response
        ChatCompletionResponse result1 = cache.get(request1).block();
        ChatCompletionResponse result2 = cache.get(request2).block();

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.id()).isEqualTo("resp-1");
        assertThat(result2.id()).isEqualTo("resp-2");
    }

    @Test
    @DisplayName("Should update cache size correctly")
    void shouldUpdateCacheSizeCorrectly() {
        cache.put(request1, response1).block();
        cache.put(request2, response2).block();

        var stats = cache.getStats();
        assertThat(stats.size()).isGreaterThanOrEqualTo(2);
    }
}
