package com.litellm.sdk.unit.budget;

import com.litellm.sdk.model.budget.CostInfo;
import com.litellm.sdk.model.common.Usage;
import com.litellm.sdk.budget.util.CostCalculator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CostCalculatorTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    @DisplayName("Test calculate cost with usage")
    public void testCalculateCostWithUsage() {
        Usage usage = Usage.of(100, 200);
        CostInfo costInfo = CostCalculator.calculateCost("gpt-4", usage);

        assertNotNull(costInfo);
        assertEquals("gpt-4", costInfo.model());
        assertEquals(100, costInfo.promptTokens());
        assertEquals(200, costInfo.completionTokens());
        assertEquals(300, costInfo.totalTokens());
        assertTrue(costInfo.totalCost() > 0);
        assertTrue(costInfo.costPerToken() > 0);
    }

    @Test
    @DisplayName("Test calculate cost with null tokens")
    public void testCalculateCostWithNullTokens() {
        CostInfo costInfo = CostCalculator.calculateCost("gpt-4", null, null);

        assertNotNull(costInfo);
        assertEquals(0.0, costInfo.totalCost());
        assertEquals(Integer.valueOf(0), costInfo.totalTokens());
    }

    @Test
    @DisplayName("Test calculate cost with unknown model")
    public void testCalculateCostWithUnknownModel() {
        CostInfo costInfo = CostCalculator.calculateCost("unknown-model", 100, 200);

        assertNotNull(costInfo);
        assertEquals("unknown-model", costInfo.model());
        assertEquals(0.0, costInfo.totalCost());
    }

    @Test
    @DisplayName("Test calculate cost with null model")
    public void testCalculateCostWithNullModel() {
        CostInfo costInfo = CostCalculator.calculateCost(null, 100, 200);

        assertNotNull(costInfo);
        assertNull(costInfo.model());
        assertEquals(0.0, costInfo.totalCost());
    }

    @Test
    @DisplayName("Test estimate cost from text")
    public void testEstimateCostFromText() {
        String inputText = "This is a test input with some content";
        String outputText = "This is a test output with more content";

        CostInfo costInfo = CostCalculator.estimateCostFromText("gpt-4", inputText, outputText);

        assertNotNull(costInfo);
        assertEquals("gpt-4", costInfo.model());
        assertTrue(costInfo.totalCost() >= 0);
    }

    @Test
    @DisplayName("Test estimate cost from text with null output")
    public void testEstimateCostFromTextWithNullOutput() {
        String inputText = "This is a test input";

        CostInfo costInfo = CostCalculator.estimateCostFromText("gpt-4", inputText, null);

        assertNotNull(costInfo);
        assertEquals("gpt-4", costInfo.model());
        assertTrue(costInfo.totalCost() >= 0);
    }

    @Test
    @DisplayName("Test estimate cost from empty text")
    public void testEstimateCostFromEmptyText() {
        CostInfo costInfo = CostCalculator.estimateCostFromText("gpt-4", "", "");

        assertNotNull(costInfo);
        assertEquals("gpt-4", costInfo.model());
        assertTrue(costInfo.totalCost() >= 0);
    }

    @Test
    @DisplayName("Test estimate cost from messages")
    public void testEstimateCostFromMessages() {
        java.util.List<String> messages = java.util.Arrays.asList(
            "Message 1",
            "Message 2",
            "Message 3"
        );

        CostInfo costInfo = CostCalculator.estimateCostFromMessages("gpt-4", messages);

        assertNotNull(costInfo);
        assertEquals("gpt-4", costInfo.model());
        assertTrue(costInfo.totalCost() >= 0);
    }

    @Test
    @DisplayName("Test estimate cost from null messages")
    public void testEstimateCostFromNullMessages() {
        CostInfo costInfo = CostCalculator.estimateCostFromMessages("gpt-4", null);

        assertNotNull(costInfo);
        assertEquals("gpt-4", costInfo.model());
        assertEquals(0.0, costInfo.totalCost());
        assertEquals(0.0, costInfo.totalCost(), 0.001);
    }

    @Test
    @DisplayName("Test has pricing for known model")
    public void testHasPricingForKnownModel() {
        assertTrue(CostCalculator.hasPricing("gpt-4"));
        assertTrue(CostCalculator.hasPricing("gpt-3.5-turbo"));
        assertTrue(CostCalculator.hasPricing("claude-3-opus"));
    }

    @Test
    @DisplayName("Test has pricing for unknown model")
    public void testHasPricingForUnknownModel() {
        assertFalse(CostCalculator.hasPricing("unknown-model-123"));
    }

    @Test
    @DisplayName("Test get pricing")
    public void testGetPricing() {
        CostCalculator.ModelPricingInfo pricing = CostCalculator.getPricingInfo("gpt-4");

        assertNotNull(pricing);
        assertTrue(pricing.inputCostPerToken() > 0);
        assertTrue(pricing.outputCostPerToken() > 0);
    }

    @Test
    @DisplayName("Test get pricing for unknown model")
    public void testGetPricingForUnknownModel() {
        CostCalculator.ModelPricingInfo pricing = CostCalculator.getPricingInfo("unknown-model");

        assertNull(pricing);
    }

    @Test
    @DisplayName("Test get registered models")
    public void testGetRegisteredModels() {
        java.util.Set<String> models = CostCalculator.getRegisteredModels();

        assertNotNull(models);
        assertTrue(models.size() > 0);
        assertTrue(models.contains("gpt-4"));
        assertTrue(models.contains("gpt-3.5-turbo"));
    }

    @Test
    @DisplayName("Test register custom pricing")
    public void testRegisterCustomPricing() {
        String modelName = "custom-model";
        double inputCost = 0.001;
        double outputCost = 0.002;

        CostCalculator.registerModelPricing(modelName, inputCost, outputCost);

        assertTrue(CostCalculator.hasPricing(modelName));

        CostCalculator.ModelPricingInfo pricing = CostCalculator.getPricingInfo(modelName);
        assertEquals(inputCost, pricing.inputCostPerToken());
        assertEquals(outputCost, pricing.outputCostPerToken());
    }

    @Test
    @DisplayName("Test register custom pricing with null model")
    public void testRegisterCustomPricingWithNullModel() {
        CostCalculator.registerModelPricing(null, 0.001, 0.002);

        assertTrue(true);
    }

    @Test
    @DisplayName("Test register custom pricing with empty model")
    public void testRegisterCustomPricingWithEmptyModel() {
        CostCalculator.registerModelPricing("", 0.001, 0.002);

        assertTrue(true);
    }

    @Test
    @DisplayName("Test remove model pricing")
    public void testRemoveModelPricing() {
        String modelName = "custom-model";
        CostCalculator.registerModelPricing(modelName, 0.001, 0.002);

        assertTrue(CostCalculator.hasPricing(modelName));

        CostCalculator.removeModelPricing(modelName);

        assertFalse(CostCalculator.hasPricing(modelName));
    }

    @Test
    @DisplayName("Test cost calculation for different models")
    public void testCostCalculationForDifferentModels() {
        int promptTokens = 1000;
        int completionTokens = 500;

        String[] models = {"gpt-4", "gpt-3.5-turbo", "claude-3-opus"};

        for (String model : models) {
            CostInfo costInfo = CostCalculator.calculateCost(model, promptTokens, completionTokens);
            assertNotNull(costInfo);
            assertTrue(costInfo.totalCost() > 0);
        }
    }

    @Test
    @DisplayName("Test ModelPricingInfo calculate total cost")
    public void testModelPricingInfoCalculateTotalCost() {
        CostCalculator.ModelPricingInfo pricing = new CostCalculator.ModelPricingInfo(0.001, 0.002);

        double totalCost = pricing.calculateTotalCost(1000, 500);

        assertEquals(1.0, totalCost, 0.001);
    }

    @Test
    @DisplayName("Test ModelPricingInfo toString")
    public void testModelPricingInfoToString() {
        CostCalculator.ModelPricingInfo pricing = new CostCalculator.ModelPricingInfo(0.001, 0.002);

        String result = pricing.toString();
        assertNotNull(result);
        assertTrue(result.contains("0.001"));
        assertTrue(result.contains("0.002"));
    }
}