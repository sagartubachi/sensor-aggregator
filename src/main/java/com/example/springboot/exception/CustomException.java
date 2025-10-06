package com.example.springboot.exception;

/*
This is a Custom Exception class that can be used when exceptions need to be thrown
manually when certain erroneous conditions are met in the service layer usually.
This extends RuntimeException intentionally to make sure all exceptions are caught.
 */
public class CustomException extends RuntimeException {

    public enum ErrorCode {
        GENERIC_ERROR,
        VALIDATION_ERROR,
        NOT_FOUND,
        MISSING_PARAMETER,
        TYPE_MISMATCH
    }

    private final ErrorCode errorCode;

    // Default constructor: generic error
    public CustomException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERIC_ERROR;
    }

    // Constructor with custom error code
    public CustomException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
