package com.litellm.sdk.config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public final class LiteLLMConfig {
    private static volatile String apiKey;
    private static volatile String apiBase;
    private static volatile String apiVersion;
    private static volatile String organization;

    private static volatile String openaiKey;
    private static volatile String anthropicKey;
    private static volatile String xaiKey;
    private static volatile String replicateKey;
    private static volatile String togetheraiKey;

    private static volatile String azureApiBase;
    private static volatile String azureApiVersion;
    private static volatile String azureApiType;

    private static volatile String openaiBaseUrl;

    private static final Map<Provider, ProviderConfigMap> providerParameters = new ConcurrentHashMap<>();

    private LiteLLMConfig() {
    }

    public static void setApiKey(String apiKey) {
        LiteLLMConfig.apiKey = apiKey;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static void setApiBase(String apiBase) {
        LiteLLMConfig.apiBase = apiBase;
    }

    public static String getApiBase() {
        return apiBase;
    }

    public static void setApiVersion(String apiVersion) {
        LiteLLMConfig.apiVersion = apiVersion;
    }

    public static String getApiVersion() {
        return apiVersion;
    }

    public static void setOrganization(String organization) {
        LiteLLMConfig.organization = organization;
    }

    public static String getOrganization() {
        return organization;
    }

    public static void setOpenaiKey(String key) {
        openaiKey = key;
    }

    public static String getOpenaiKey() {
        return openaiKey;
    }

    public static void setAnthropicKey(String key) {
        anthropicKey = key;
    }

    public static String getAnthropicKey() {
        return anthropicKey;
    }

    public static void setXaiKey(String key) {
        xaiKey = key;
    }

    public static String getXaiKey() {
        return xaiKey;
    }

    public static void setReplicateKey(String key) {
        replicateKey = key;
    }

    public static String getReplicateKey() {
        return replicateKey;
    }

    public static void setTogetheraiKey(String key) {
        togetheraiKey = key;
    }

    public static String getTogetheraiKey() {
        return togetheraiKey;
    }

    public static void setAzureApiBase(String baseUrl) {
        azureApiBase = baseUrl;
    }

    public static String getAzureApiBase() {
        return azureApiBase;
    }

    public static void setAzureApiVersion(String version) {
        azureApiVersion = version;
    }

    public static String getAzureApiVersion() {
        return azureApiVersion;
    }

    public static void setAzureApiType(String type) {
        azureApiType = type;
    }

    public static String getAzureApiType() {
        return azureApiType;
    }

    public static void setOpenaiBaseUrl(String baseUrl) {
        openaiBaseUrl = baseUrl;
    }

    public static String getOpenaiBaseUrl() {
        return openaiBaseUrl;
    }

    public static void setProviderParameters(Provider provider, String project, String regionName, String token) {
        providerParameters.put(provider, new ProviderConfigMap(project, regionName, token));
    }

    public static ProviderConfigMap getProviderParameters(Provider provider) {
        return providerParameters.get(provider);
    }

    public static void loadFromEnvironment() {
        String openaiKey = System.getenv("OPENAI_API_KEY");
        if (openaiKey != null && !openaiKey.trim().isEmpty()) {
            setOpenaiKey(openaiKey);
        }

        String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
        if (anthropicKey != null && !anthropicKey.trim().isEmpty()) {
            setAnthropicKey(anthropicKey);
        }

        String xaiKey = System.getenv("XAI_API_KEY");
        if (xaiKey != null && !xaiKey.trim().isEmpty()) {
            setXaiKey(xaiKey);
        }

        String replicateKey = System.getenv("REPLICATE_API_KEY");
        if (replicateKey != null && !replicateKey.trim().isEmpty()) {
            setReplicateKey(replicateKey);
        }

        String togetheraiKey = System.getenv("TOGETHERAI_API_KEY");
        if (togetheraiKey != null && !togetheraiKey.trim().isEmpty()) {
            setTogetheraiKey(togetheraiKey);
        }

        String azureApiBase = System.getenv("AZURE_API_BASE");
        if (azureApiBase != null && !azureApiBase.trim().isEmpty()) {
            setAzureApiBase(azureApiBase);
        }

        String azureApiVersion = System.getenv("AZURE_API_VERSION");
        if (azureApiVersion != null && !azureApiVersion.trim().isEmpty()) {
            setAzureApiVersion(azureApiVersion);
        }

        String azureApiType = System.getenv("AZURE_API_TYPE");
        if (azureApiType != null && !azureApiType.trim().isEmpty()) {
            setAzureApiType(azureApiType);
        }

        String openaiBaseUrl = System.getenv("OPENAI_BASE_URL");
        if (openaiBaseUrl != null && !openaiBaseUrl.trim().isEmpty()) {
            setOpenaiBaseUrl(openaiBaseUrl);
        }
    }

    public static void clear() {
        apiKey = null;
        apiBase = null;
        apiVersion = null;
        organization = null;
        openaiKey = null;
        anthropicKey = null;
        xaiKey = null;
        replicateKey = null;
        togetheraiKey = null;
        azureApiBase = null;
        azureApiVersion = null;
        azureApiType = null;
        openaiBaseUrl = null;
        providerParameters.clear();
    }

    public record ProviderConfigMap(String project, String regionName, String token) {
        @Override
        public String toString() {
            return "ProviderConfigMap{" +
                    "project='" + project + '\'' +
                    ", regionName='" + regionName + '\'' +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

    public enum Provider {
        WATSON,
        VERTEX_AI,
        AZURE,
        BEDROCK
    }

    public static Map<String, Double> getModelCost() {
        return com.litellm.sdk.token.CostCalculator.getModelCost();
    }

    public static int getMaxTokens(String model) {
        return com.litellm.sdk.token.CostCalculator.getMaxTokens(model);
    }

    public static boolean hasPricing(String model) {
        return com.litellm.sdk.token.CostCalculator.hasPricing(model);
    }

    public static Set<String> getRegisteredModels() {
        return com.litellm.sdk.token.CostCalculator.getRegisteredModels();
    }

    public static void registerModelPricing(String model, double inputCost, double outputCost) {
        com.litellm.sdk.token.CostCalculator.registerModelPricing(model, inputCost, outputCost);
    }
}
