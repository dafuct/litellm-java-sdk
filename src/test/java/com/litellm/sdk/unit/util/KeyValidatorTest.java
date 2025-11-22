package com.litellm.sdk.unit.util;

import com.litellm.sdk.config.LiteLLMConfig;
import com.litellm.sdk.config.ProviderConfig;
import com.litellm.sdk.util.KeyValidator;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KeyValidatorTest {

    @BeforeEach
    public void setUp() {
        LiteLLMConfig.clear();
    }

    @AfterEach
    public void tearDown() {
        LiteLLMConfig.clear();
    }

    @Test
    public void testCheckValidKey_WithValidFormat() {
        String validKey = "sk-" + "a".repeat(32);
        boolean result = KeyValidator.checkValidKey("gpt-3.5-turbo", validKey);
        assertFalse(result);
    }

    @Test
    public void testCheckValidKey_WithInvalidFormat() {
        String invalidKey = "invalid-key";
        boolean result = KeyValidator.checkValidKey("gpt-3.5-turbo", invalidKey);
        assertFalse(result);
    }

    @Test
    public void testCheckValidKey_WithNullKey() {
        boolean result = KeyValidator.checkValidKey("gpt-3.5-turbo", null);
        assertFalse(result);
    }

    @Test
    public void testCheckValidKey_WithEmptyKey() {
        boolean result = KeyValidator.checkValidKey("gpt-3.5-turbo", "");
        assertFalse(result);
    }

    @Test
    public void testCheckValidKey_WithTimeout() {
        String validKey = "sk-" + "b".repeat(32);
        boolean result = KeyValidator.checkValidKey("gpt-3.5-turbo", validKey, Duration.ofSeconds(1));
        assertFalse(result);
    }

    @Test
    public void testValidateAllKeys_WithNoKeysConfigured() {
        List<KeyValidator.KeyValidationResult> results = KeyValidator.validateAllKeys();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testValidateAllKeys_WithKeysConfigured() {
        LiteLLMConfig.setOpenaiKey("test-openai-key");
        LiteLLMConfig.setAnthropicKey("test-anthropic-key");

        List<KeyValidator.KeyValidationResult> results = KeyValidator.validateAllKeys();

        assertNotNull(results);
        assertEquals(2, results.size());

        KeyValidator.KeyValidationResult openaiResult = results.stream()
            .filter(r -> r.provider().equals("openai"))
            .findFirst()
            .orElse(null);

        assertNotNull(openaiResult);
        assertFalse(openaiResult.valid());
    }

    @Test
    public void testCheckValidKey_WithProviderConfig() {
        ProviderConfig config = ProviderConfig.builder()
            .id("test-provider")
            .name("test")
            .apiKey("test-key")
            .baseUrl("https://test.com")
            .models(List.of("gpt-3.5-turbo"))
            .weight(1)
            .build();

        boolean result = KeyValidator.checkValidKey(config);
        assertFalse(result);
    }

    @Test
    public void testCheckValidKey_WithNullConfig() {
        boolean result = KeyValidator.checkValidKey((ProviderConfig) null);
        assertFalse(result);
    }

    @Test
    public void testCheckValidKey_WithConfigWithoutApiKey() {
        ProviderConfig config = ProviderConfig.builder()
            .id("test-provider")
            .name("test")
            .baseUrl("https://test.com")
            .models(List.of("gpt-3.5-turbo"))
            .weight(1)
            .build();

        boolean result = KeyValidator.checkValidKey(config);
        assertFalse(result);
    }

    @Test
    public void testKeyValidationResult_GetMaskedKey() {
        String apiKey = "sk-test1234567890abcdef";
        KeyValidator.KeyValidationResult result =
            new KeyValidator.KeyValidationResult("openai", apiKey, true, "openai");

        String maskedKey = result.getMaskedKey();
        assertNotNull(maskedKey);
        assertTrue(maskedKey.contains("*"));
        assertNotEquals(apiKey, maskedKey);
    }

    @Test
    public void testKeyValidationResult_GetMaskedKey_ShortKey() {
        String apiKey = "short";
        KeyValidator.KeyValidationResult result =
            new KeyValidator.KeyValidationResult("openai", apiKey, true, "openai");

        String maskedKey = result.getMaskedKey();
        assertEquals("***", maskedKey);
    }

    @Test
    public void testKeyValidationResult_ToString() {
        String apiKey = "sk-test1234567890abcdef";
        KeyValidator.KeyValidationResult result =
            new KeyValidator.KeyValidationResult("openai", apiKey, true, "openai");

        String str = result.toString();
        assertNotNull(str);
        assertTrue(str.contains("openai"));
        assertTrue(str.contains("valid=true"));
    }
}
