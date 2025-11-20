package com.litellm.sdk.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LiteLLMException extends Exception {
    public final String errorCode;
    public final String correlationId;
    public final String actionableMessage;

    protected LiteLLMException(String message, String errorCode, String correlationId, String actionableMessage) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
        this.actionableMessage = actionableMessage;
    }

    protected LiteLLMException(String message, Throwable cause, String errorCode, String correlationId, String actionableMessage) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
        this.actionableMessage = actionableMessage;
    }
}
