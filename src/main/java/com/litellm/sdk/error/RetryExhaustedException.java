package com.litellm.sdk.error;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class RetryExhaustedException extends LiteLLMException {
    public final int maxAttempts;
    public final int actualAttempts;
    public final String lastErrorMessage;

    public RetryExhaustedException(String message, int maxAttempts, int actualAttempts, String lastErrorMessage) {
        super(message, "RETRY_EXHAUSTED", null, "Consider increasing retry attempts or checking provider status");
        this.maxAttempts = maxAttempts;
        this.actualAttempts = actualAttempts;
        this.lastErrorMessage = lastErrorMessage;
    }

    public RetryExhaustedException(String message, Throwable cause, int maxAttempts, int actualAttempts, String lastErrorMessage) {
        super(message, cause, "RETRY_EXHAUSTED", null, "Consider increasing retry attempts or checking provider status");
        this.maxAttempts = maxAttempts;
        this.actualAttempts = actualAttempts;
        this.lastErrorMessage = lastErrorMessage;
    }
}
