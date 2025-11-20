package com.litellm.sdk.model.common;

import lombok.Builder;

import java.time.Instant;

@Builder(toBuilder = true)
public record ErrorInfo(String errorType, String errorCode, String message, String actionableMessage,
                        String correlationId, String providerId, Boolean retryable, Integer retryAttempt,
                        Instant timestamp, Throwable cause) {
    public ErrorInfo(
            String errorType,
            String errorCode,
            String message,
            String actionableMessage,
            String correlationId,
            String providerId,
            Boolean retryable,
            Integer retryAttempt,
            Instant timestamp,
            Throwable cause
    ) {
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.message = message;
        this.actionableMessage = actionableMessage;
        this.correlationId = correlationId;
        this.providerId = providerId;
        this.retryable = retryable;
        this.retryAttempt = retryAttempt;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.cause = cause;
    }

    public static ErrorInfo of(
            String errorType,
            String errorCode,
            String message,
            String actionableMessage,
            String correlationId
    ) {
        return new ErrorInfo(errorType, errorCode, message, actionableMessage, correlationId, null, null, null, null, null);
    }
}
