package com.litellm.sdk.error;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ValidationException extends LiteLLMException {
    public final String fieldName;
    public final String fieldValue;

    public ValidationException(String message, String fieldName, String fieldValue) {
        super(message, "VALIDATION_ERROR", null, "Check the request parameters and ensure all required fields are provided with valid values");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ValidationException(String message, Throwable cause, String fieldName, String fieldValue) {
        super(message, cause, "VALIDATION_ERROR", null, "Check the request parameters and ensure all required fields are provided with valid values");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
