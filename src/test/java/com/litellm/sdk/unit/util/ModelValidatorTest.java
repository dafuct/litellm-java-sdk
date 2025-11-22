package com.litellm.sdk.unit.util;

import com.litellm.sdk.config.LiteLLMConfig;
import com.litellm.sdk.util.ModelValidator;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModelValidatorTest {

    @BeforeEach
    public void setUp() {
        LiteLLMConfig.clear();
    }

    @AfterEach
    public void tearDown() {
        LiteLLMConfig.clear();
    }

    @Test
    public void testGetValidModels_WithNoKeys() {
        List<String> models = ModelValidator.getValidModels();
        assertNotNull(models);
        assertTrue(models.isEmpty());
    }

    @Test
    public void testGetValidModels_WithOpenAIKey() {
        LiteLLMConfig.setOpenaiKey("test-key");

        List<String> models = ModelValidator.getValidModels();
        assertNotNull(models);
        assertFalse(models.isEmpty());
        assertTrue(models.contains("gpt-3.5-turbo"));
        assertTrue(models.contains("gpt-4"));
    }

    @Test
    public void testGetValidModels_WithAnthropicKey() {
        LiteLLMConfig.setAnthropicKey("test-key");

        List<String> models = ModelValidator.getValidModels();
        assertNotNull(models);
        assertFalse(models.isEmpty());
        assertTrue(models.contains("claude-2"));
        assertTrue(models.contains("claude-3-opus"));
    }

    @Test
    public void testGetValidModels_WithMultipleKeys() {
        LiteLLMConfig.setOpenaiKey("test-key");
        LiteLLMConfig.setAnthropicKey("test-key");
        LiteLLMConfig.setXaiKey("test-key");

        List<String> models = ModelValidator.getValidModels();
        assertNotNull(models);
        assertFalse(models.isEmpty());

        assertTrue(models.contains("gpt-3.5-turbo"));
        assertTrue(models.contains("claude-2"));
        assertTrue(models.contains("grok-beta"));
    }

    @Test
    public void testGetValidModels_WithCheckEndpoint() {
        LiteLLMConfig.setOpenaiKey("test-key");

        List<String> models = ModelValidator.getValidModels(true);
        assertNotNull(models);
    }

    @Test
    public void testGetValidModels_ForProvider() {
        List<String> openaiModels = ModelValidator.getValidModels("openai");
        assertNotNull(openaiModels);
        assertTrue(openaiModels.contains("gpt-3.5-turbo"));

        List<String> anthropicModels = ModelValidator.getValidModels("anthropic");
        assertNotNull(anthropicModels);
        assertTrue(anthropicModels.contains("claude-2"));
    }

    @Test
    public void testGetValidModels_ForUnknownProvider() {
        List<String> models = ModelValidator.getValidModels("unknown-provider");
        assertNotNull(models);
    }

    @Test
    public void testGetValidModels_ForProvider_WithCheckEndpoint() {
        List<String> models = ModelValidator.getValidModels("openai", true);
        assertNotNull(models);
        assertTrue(models.contains("gpt-3.5-turbo"));
    }

    @Test
    public void testGetValidModels_WithNullProvider() {
        List<String> models = ModelValidator.getValidModels(null);
        assertNotNull(models);
    }

    @Test
    public void testIsValidModel_WithValidModel() {
        LiteLLMConfig.setOpenaiKey("test-key");

        boolean result = ModelValidator.isValidModel("gpt-3.5-turbo");
        assertTrue(result);
    }

    @Test
    public void testIsValidModel_WithInvalidModel() {
        boolean result = ModelValidator.isValidModel("invalid-model-xyz");
        assertFalse(result);
    }

    @Test
    public void testIsValidModel_WithNull() {
        boolean result = ModelValidator.isValidModel(null);
        assertFalse(result);
    }

    @Test
    public void testGetModelsByProvider() {
        LiteLLMConfig.setOpenaiKey("test-key");
        LiteLLMConfig.setAnthropicKey("test-key");

        Map<String, List<String>> grouped = ModelValidator.getModelsByProvider();

        assertNotNull(grouped);
        assertTrue(grouped.containsKey("openai"));
        assertTrue(grouped.containsKey("anthropic"));

        List<String> openaiModels = grouped.get("openai");
        assertNotNull(openaiModels);
        assertTrue(openaiModels.contains("gpt-3.5-turbo"));
    }

    @Test
    public void testGetProviderForModel() {
        LiteLLMConfig.setOpenaiKey("test-key");
        LiteLLMConfig.setAnthropicKey("test-key");

        String provider = ModelValidator.getProviderForModel("gpt-3.5-turbo");
        assertEquals("openai", provider);

        String claudeProvider = ModelValidator.getProviderForModel("claude-2");
        assertEquals("anthropic", claudeProvider);
    }

    @Test
    public void testGetProviderForModel_UnknownModel() {
        String provider = ModelValidator.getProviderForModel("unknown-model");
        assertEquals("unknown", provider);
    }

    @Test
    public void testGetProviderForModel_WithNull() {
        String provider = ModelValidator.getProviderForModel(null);
        assertEquals("unknown", provider);
    }
}
