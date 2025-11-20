package com.litellm.sdk.cache;

import com.litellm.sdk.model.request.ChatCompletionRequest;
import com.litellm.sdk.model.response.ChatCompletionResponse;
import reactor.core.publisher.Mono;

public interface CacheManager {
    Mono<ChatCompletionResponse> get(ChatCompletionRequest request);

    Mono<Void> put(ChatCompletionRequest request, ChatCompletionResponse response);

    void invalidate(ChatCompletionRequest request);

    void invalidateAll();

    CacheStats getStats();

    void close();

    record CacheStats(long hitCount, long missCount, long evictionCount, int size) {

        public double getHitRate() {
                long total = hitCount + missCount;
                return total > 0 ? (double) hitCount / total : 0.0;
            }
        }
}
