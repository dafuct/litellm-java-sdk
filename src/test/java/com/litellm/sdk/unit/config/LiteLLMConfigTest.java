package com.litellm.sdk.unit.config;

import com.litellm.sdk.config.LiteLLMConfig;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LiteLLMConfigTest {

    @BeforeEach
    public void setUp() {
        LiteLLMConfig.clear();
    }

    @AfterEach
    public void tearDown() {
        LiteLLMConfig.clear();
    }

    @Test
    public void testSetAndGetApiKey() {
        String testKey = "test-api-key";
        LiteLLMConfig.setApiKey(testKey);
        assertEquals(testKey, LiteLLMConfig.getApiKey());
    }

    @Test
    public void testSetAndGetApiBase() {
        String testBase = "https://api.test.com";
        LiteLLMConfig.setApiBase(testBase);
        assertEquals(testBase, LiteLLMConfig.getApiBase());
    }

    @Test
    public void testSetAndGetApiVersion() {
        String testVersion = "2023-05-15";
        LiteLLMConfig.setApiVersion(testVersion);
        assertEquals(testVersion, LiteLLMConfig.getApiVersion());
    }

    @Test
    public void testSetAndGetOrganization() {
        String testOrg = "test-org";
        LiteLLMConfig.setOrganization(testOrg);
        assertEquals(testOrg, LiteLLMConfig.getOrganization());
    }

    @Test
    public void testProviderSpecificKeys() {
        LiteLLMConfig.setOpenaiKey("openai-key");
        LiteLLMConfig.setAnthropicKey("anthropic-key");
        LiteLLMConfig.setXaiKey("xai-key");
        LiteLLMConfig.setReplicateKey("replicate-key");
        LiteLLMConfig.setTogetheraiKey("together-key");

        assertEquals("openai-key", LiteLLMConfig.getOpenaiKey());
        assertEquals("anthropic-key", LiteLLMConfig.getAnthropicKey());
        assertEquals("xai-key", LiteLLMConfig.getXaiKey());
        assertEquals("replicate-key", LiteLLMConfig.getReplicateKey());
        assertEquals("together-key", LiteLLMConfig.getTogetheraiKey());
    }

    @Test
    public void testAzureConfiguration() {
        String azureBase = "https://test.azure.com/";
        String azureVersion = "2023-05-15";
        String azureType = "azure";

        LiteLLMConfig.setAzureApiBase(azureBase);
        LiteLLMConfig.setAzureApiVersion(azureVersion);
        LiteLLMConfig.setAzureApiType(azureType);

        assertEquals(azureBase, LiteLLMConfig.getAzureApiBase());
        assertEquals(azureVersion, LiteLLMConfig.getAzureApiVersion());
        assertEquals(azureType, LiteLLMConfig.getAzureApiType());
    }

    @Test
    public void testOpenaiBaseUrlOverride() {
        String customUrl = "https://custom.openai.endpoint";
        LiteLLMConfig.setOpenaiBaseUrl(customUrl);
        assertEquals(customUrl, LiteLLMConfig.getOpenaiBaseUrl());
    }

    @Test
    public void testProviderParameters() {
        String project = "test-project";
        String region = "us-east-1";
        String token = "test-token";

        LiteLLMConfig.setProviderParameters(
            LiteLLMConfig.Provider.WATSON,
            project,
            region,
            token
        );

        LiteLLMConfig.ProviderConfigMap params =
            LiteLLMConfig.getProviderParameters(LiteLLMConfig.Provider.WATSON);

        assertNotNull(params);
        assertEquals(project, params.project());
        assertEquals(region, params.regionName());
        assertEquals(token, params.token());
    }

    @Test
    public void testLoadFromEnvironment() {
        System.setProperty("OPENAI_API_KEY", "env-openai-key");
        System.setProperty("ANTHROPIC_API_KEY", "env-anthropic-key");
        System.setProperty("XAI_API_KEY", "env-xai-key");
        System.setProperty("REPLICATE_API_KEY", "env-replicate-key");
        System.setProperty("TOGETHERAI_API_KEY", "env-together-key");

        LiteLLMConfig.loadFromEnvironment();

        assertEquals("env-openai-key", LiteLLMConfig.getOpenaiKey());
        assertEquals("env-anthropic-key", LiteLLMConfig.getAnthropicKey());
        assertEquals("env-xai-key", LiteLLMConfig.getXaiKey());
        assertEquals("env-replicate-key", LiteLLMConfig.getReplicateKey());
        assertEquals("env-together-key", LiteLLMConfig.getTogetheraiKey());
    }

    @Test
    public void testClear() {
        LiteLLMConfig.setApiKey("test-key");
        LiteLLMConfig.setOpenaiKey("openai-key");
        LiteLLMConfig.setProviderParameters(
            LiteLLMConfig.Provider.WATSON,
            "project",
            "region",
            "token"
        );

        LiteLLMConfig.clear();

        assertNull(LiteLLMConfig.getApiKey());
        assertNull(LiteLLMConfig.getOpenaiKey());
        assertNull(LiteLLMConfig.getProviderParameters(LiteLLMConfig.Provider.WATSON));
    }

    @Test
    public void testProviderConfigMapToString() {
        LiteLLMConfig.ProviderConfigMap params =
            new LiteLLMConfig.ProviderConfigMap("project", "region", "token");

        String str = params.toString();
        assertNotNull(str);
        assertTrue(str.contains("project"));
        assertTrue(str.contains("region"));
        assertTrue(str.contains("token"));
    }
}
