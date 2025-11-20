package com.litellm.sdk.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ProviderException extends LiteLLMException {
    public final String providerId;
    public final int httpStatusCode;
    public final String providerErrorCode;

    public ProviderException(String message, String providerId, int httpStatusCode) {
        super(message, "PROVIDER_ERROR", null, "Check the provider configuration and request parameters");
        this.providerId = providerId;
        this.httpStatusCode = httpStatusCode;
        this.providerErrorCode = null;
    }

    public ProviderException(String message, String providerId, int httpStatusCode, String providerErrorCode) {
        super(message, "PROVIDER_ERROR", null, "Check the provider configuration and request parameters");
        this.providerId = providerId;
        this.httpStatusCode = httpStatusCode;
        this.providerErrorCode = providerErrorCode;
    }

    public ProviderException(String message, Throwable cause, String providerId, int httpStatusCode, String providerErrorCode) {
        super(message, cause, "PROVIDER_ERROR", null, "Check the provider configuration and request parameters");
        this.providerId = providerId;
        this.httpStatusCode = httpStatusCode;
        this.providerErrorCode = providerErrorCode;
    }

    public boolean isRetryable() {
        return httpStatusCode == 429 || httpStatusCode >= 500;
    }
}
