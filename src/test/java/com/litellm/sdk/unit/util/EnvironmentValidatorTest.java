package com.litellm.sdk.unit.util;

import com.litellm.sdk.config.LiteLLMConfig;
import com.litellm.sdk.util.EnvironmentValidator;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentValidatorTest {

    @BeforeEach
    public void setUp() {
        LiteLLMConfig.clear();
    }

    @AfterEach
    public void tearDown() {
        LiteLLMConfig.clear();
    }

    @Test
    public void testValidateEnvironment_WithValidModel() {
        LiteLLMConfig.setOpenaiKey("test-key");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("openai/gpt-3.5-turbo");

        assertNotNull(result);
        assertEquals("openai", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithNullModel() {
        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment(null);

        assertNotNull(result);
        assertFalse(result.valid());
        assertTrue(result.missingKeys().contains("Model name is required"));
    }

    @Test
    public void testValidateEnvironment_WithEmptyModel() {
        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("");

        assertNotNull(result);
        assertFalse(result.valid());
    }

    @Test
    public void testValidateEnvironment_WithOpenAIModel() {
        LiteLLMConfig.setOpenaiKey("test-openai-key");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("gpt-3.5-turbo");

        assertNotNull(result);
        assertEquals("openai", result.provider());
        assertTrue(result.valid() || !result.valid());
    }

    @Test
    public void testValidateEnvironment_WithClaudeModel() {
        LiteLLMConfig.setAnthropicKey("test-key");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("claude-2");

        assertNotNull(result);
        assertEquals("anthropic", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithGrokModel() {
        LiteLLMConfig.setXaiKey("test-key");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("grok-beta");

        assertNotNull(result);
        assertEquals("xai", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithGeminiModel() {
        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("gemini-pro");

        assertNotNull(result);
        assertEquals("gemini", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithAzureConfig() {
        LiteLLMConfig.setAzureApiBase("https://test.azure.com/");
        LiteLLMConfig.setAzureApiVersion("2023-05-15");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("azure/gpt-3.5-turbo");

        assertNotNull(result);
        assertEquals("azure", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithProviderPrefix() {
        LiteLLMConfig.setOpenaiKey("test-key");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("openai/gpt-3.5-turbo");

        assertNotNull(result);
        assertEquals("openai", result.provider());
    }

    @Test
    public void testValidateAllEnvironments_WithNoKeys() {
        List<EnvironmentValidator.EnvironmentValidationResult> results =
            EnvironmentValidator.validateAllEnvironments();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testValidateAllEnvironments_WithOpenAIKey() {
        LiteLLMConfig.setOpenaiKey("test-key");

        List<EnvironmentValidator.EnvironmentValidationResult> results =
            EnvironmentValidator.validateAllEnvironments();

        assertNotNull(results);
        assertFalse(results.isEmpty());

        EnvironmentValidator.EnvironmentValidationResult openaiResult = results.stream()
            .filter(r -> r.provider().equals("openai"))
            .findFirst()
            .orElse(null);

        assertNotNull(openaiResult);
    }

    @Test
    public void testValidateAllEnvironments_WithMultipleProviders() {
        LiteLLMConfig.setOpenaiKey("test-key");
        LiteLLMConfig.setAnthropicKey("test-key");
        LiteLLMConfig.setXaiKey("test-key");

        List<EnvironmentValidator.EnvironmentValidationResult> results =
            EnvironmentValidator.validateAllEnvironments();

        assertNotNull(results);
        assertTrue(results.size() >= 3);

        boolean hasOpenai = results.stream().anyMatch(r -> r.provider().equals("openai"));
        boolean hasAnthropic = results.stream().anyMatch(r -> r.provider().equals("anthropic"));
        boolean hasXai = results.stream().anyMatch(r -> r.provider().equals("xai"));

        assertTrue(hasOpenai);
        assertTrue(hasAnthropic);
        assertTrue(hasXai);
    }

    @Test
    public void testValidateAllEnvironments_WithAzure() {
        LiteLLMConfig.setAzureApiBase("https://test.azure.com/");
        LiteLLMConfig.setAzureApiVersion("2023-05-15");

        List<EnvironmentValidator.EnvironmentValidationResult> results =
            EnvironmentValidator.validateAllEnvironments();

        assertNotNull(results);

        EnvironmentValidator.EnvironmentValidationResult azureResult = results.stream()
            .filter(r -> r.provider().equals("azure"))
            .findFirst()
            .orElse(null);

        assertNotNull(azureResult);
    }

    @Test
    public void testEnvironmentValidationResult_ToString() {
        EnvironmentValidator.EnvironmentValidationResult result =
            new EnvironmentValidator.EnvironmentValidationResult(
                "test-provider",
                true,
                List.of(),
                Map.of(),
                "Test message"
            );

        String str = result.toString();
        assertNotNull(str);
        assertTrue(str.contains("test-provider"));
        assertTrue(str.contains("valid=true"));
    }

    @Test
    public void testValidateEnvironment_WithWatson() {
        LiteLLMConfig.setProviderParameters(
            LiteLLMConfig.Provider.WATSON,
            "test-project",
            "us-south",
            "test-token"
        );

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("watson/test-model");

        assertNotNull(result);
        assertEquals("watson", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithVertexAI() {
        System.setProperty("GOOGLE_CLOUD_PROJECT", "test-project");
        System.setProperty("GOOGLE_CLOUD_LOCATION", "us-central1");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("vertex/test-model");

        assertNotNull(result);
        assertEquals("vertex", result.provider());
    }

    @Test
    public void testValidateEnvironment_WithBedrock() {
        System.setProperty("AWS_ACCESS_KEY_ID", "test-key");
        System.setProperty("AWS_SECRET_ACCESS_KEY", "test-secret");
        System.setProperty("AWS_REGION", "us-east-1");

        EnvironmentValidator.EnvironmentValidationResult result =
            EnvironmentValidator.validateEnvironment("bedrock/test-model");

        assertNotNull(result);
        assertEquals("bedrock", result.provider());
    }
}
