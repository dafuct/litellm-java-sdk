package com.litellm.sdk.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.litellm.sdk.config.CacheConfig;
import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

public class CaffeineCache implements CacheManager {
    private final Cache<String, ChatCompletionResponse> cache;
    private final CacheConfig config;
    private volatile long hitCount = 0;
    private volatile long missCount = 0;
    private volatile long evictionCount = 0;

    public CaffeineCache(CacheConfig config) {
        this.config = config;

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
            .maximumSize(config.maxSize())
            .recordStats();

        Duration ttl = config.ttl();
        if (config.timeUnit() == TimeUnit.SECONDS) {
            caffeine.expireAfterWrite(ttl);
        } else {
            long durationInSeconds = config.timeUnit().toSeconds(ttl.getSeconds());
            caffeine.expireAfterWrite(durationInSeconds, TimeUnit.SECONDS);
        }

        this.cache = caffeine
            .evictionListener((key, value, cause) -> evictionCount++)
            .build();
    }

    @Override
    public Mono<ChatCompletionResponse> get(ChatCompletionRequest request) {
        return Mono.defer(() -> {
            String key = generateCacheKey(request);
            ChatCompletionResponse response = cache.getIfPresent(key);

            if (response != null) {
                hitCount++;
                return Mono.just(response);
            } else {
                missCount++;
                return Mono.empty();
            }
        });
    }

    @Override
    public Mono<Void> put(ChatCompletionRequest request, ChatCompletionResponse response) {
        return Mono.fromRunnable(() -> {
            String key = generateCacheKey(request);
            cache.put(key, response);
        });
    }

    @Override
    public void invalidate(ChatCompletionRequest request) {
        String key = generateCacheKey(request);
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public CacheStats getStats() {
        return new CacheStats(hitCount, missCount, evictionCount, (int) cache.estimatedSize());
    }

    @Override
    public void close() {
        cache.cleanUp();
    }

    private String generateCacheKey(ChatCompletionRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.model());

        if (request.messages() != null) {
            for (var msg : request.messages()) {
                sb.append(msg.role()).append(":").append(msg.content());
            }
        }

        if (request.temperature() != null) {
            sb.append("temp:").append(request.temperature());
        }
        if (request.maxTokens() != null) {
            sb.append("max:").append(request.maxTokens());
        }
        if (request.topP() != null) {
            sb.append("topp:").append(request.topP());
        }

        return Objects.hash(sb.toString()) + ":" + sb.toString();
    }
}
