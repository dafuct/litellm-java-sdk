package com.litellm.sdk.client;

import com.litellm.sdk.config.*;
import com.litellm.sdk.provider.Provider;
import com.litellm.sdk.provider.LiteLLMProvider;
import com.litellm.sdk.routing.Router;
import com.litellm.sdk.routing.strategy.RoundRobinStrategy;
import lombok.Builder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Builder
public class ClientBuilder {
    @Builder.Default
    List<ProviderConfig> providers = new ArrayList<>();
    @Builder.Default
    RoutingStrategyConfig routingStrategy = RoutingStrategyConfig.builder()
        .type(RoutingStrategyConfig.StrategyType.ROUND_ROBIN)
        .build();
    @Builder.Default
    CacheConfig cache = CacheConfig.builder().build();
    @Builder.Default
    RetryConfig retry = RetryConfig.builder().build();
    @Builder.Default
    Duration timeout = Duration.ofSeconds(30);
    @Builder.Default
    String environmentPrefix = "LITELLM";

    public ClientBuilder() {
        this.providers = new ArrayList<>();
    }

    public ClientBuilder(List<ProviderConfig> providers, RoutingStrategyConfig routingStrategy, CacheConfig cache, RetryConfig retry, Duration timeout, String environmentPrefix) {
        this.providers = providers != null ? providers : new ArrayList<>();
        this.routingStrategy = routingStrategy != null ? routingStrategy : RoutingStrategyConfig.builder()
            .type(RoutingStrategyConfig.StrategyType.ROUND_ROBIN)
            .build();
        this.cache = cache != null ? cache : CacheConfig.builder().build();
        this.retry = retry != null ? retry : RetryConfig.builder().build();
        this.timeout = timeout != null ? timeout : Duration.ofSeconds(30);
        this.environmentPrefix = environmentPrefix != null ? environmentPrefix : "LITELLM";
    }

    public ClientBuilder withProvider(ProviderConfig config) {
        if (config != null) {
            this.providers.add(config);
        }
        return this;
    }

    public ClientBuilder withRoutingStrategy(RoutingStrategyConfig config) {
        this.routingStrategy = config;
        return this;
    }

    public ClientBuilder withCache(CacheConfig config) {
        this.cache = config;
        return this;
    }

    public ClientBuilder withRetryPolicy(RetryConfig config) {
        this.retry = config;
        return this;
    }

    public ClientBuilder withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public ClientBuilder withEnvironmentPrefix(String prefix) {
        this.environmentPrefix = prefix;
        return this;
    }

    public LiteLLMClient build() {
        ClientConfig config = ClientConfig.builder()
            .providers(providers)
            .routingStrategy(routingStrategy)
            .cache(cache)
            .retry(retry)
            .timeout(timeout)
            .environmentPrefix(environmentPrefix)
            .build();

        config.validate();

        List<Provider> providerInstances = providers.stream()
            .map(this::createProvider)
            .toList();

        RoundRobinStrategy strategy = new RoundRobinStrategy(routingStrategy);
        Router router = new Router(config, providerInstances, strategy);

        return new LiteLLMClient(router);
    }

    private Provider createProvider(ProviderConfig config) {
        // Use a single generic provider for all configurations
        return new LiteLLMProvider(config);
    }

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }
}
