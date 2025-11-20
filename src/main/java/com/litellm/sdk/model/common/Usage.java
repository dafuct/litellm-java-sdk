package com.litellm.sdk.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usage {
    Integer promptTokens;
    Integer completionTokens;
    Integer totalTokens;

    @JsonCreator
    public Usage(
        @JsonProperty("prompt_tokens") Integer promptTokens,
        @JsonProperty("completion_tokens") Integer completionTokens,
        @JsonProperty("total_tokens") Integer totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens != null ? totalTokens :
            ((promptTokens != null ? promptTokens : 0) + (completionTokens != null ? completionTokens : 0));
    }

    public static Usage of(Integer promptTokens, Integer completionTokens) {
        return new Usage(promptTokens, completionTokens, null);
    }

    public static Usage ofTotal(Integer totalTokens) {
        return new Usage(null, null, totalTokens);
    }
}
