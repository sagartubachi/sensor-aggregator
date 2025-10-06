package com.example.springboot.exception;

import com.example.springboot.controller.QueryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
This is a global exception handler. It catches manually thrown exceptions as well as exceptions thrown by Spring.
It also converts the exception into a standard response containing error code and error message for standardisation of error responses.
This makes sure that no internal stack traces are exposed in the response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle @Valid / @Validated validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {

        log.info("GlobalExceptionHandler : MethodArgumentNotValidException caught");

        // Extract field errors
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage()
                ));

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", CustomException.ErrorCode.VALIDATION_ERROR,
                "errors", errors
        ));
    }

    // Handle type mismatches (e.g., parsing Instant from string)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        log.info("GlobalExceptionHandler : MethodArgumentTypeMismatchException caught");

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", CustomException.ErrorCode.TYPE_MISMATCH,
                "message", String.format("Parameter '%s' has invalid value '%s'",
                        ex.getName(), ex.getValue())
        ));
    }

    // Handle missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {

        log.info("GlobalExceptionHandler : MissingServletRequestParameterException caught");

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", CustomException.ErrorCode.MISSING_PARAMETER,
                "message", String.format("Required parameter '%s' is missing", ex.getParameterName())
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFormat(HttpMessageNotReadableException ex) {

        log.info("GlobalExceptionHandler : handleInvalidFormat caught");

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", CustomException.ErrorCode.VALIDATION_ERROR,
                "message", String.format("Invalid value for field")
        ));
    }

    // Handle custom exceptions
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleApiException(CustomException ex) {

        log.info("GlobalExceptionHandler : CustomException caught");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "errorCode", ex.getErrorCode(),
                "message", ex.getMessage()
        ));
    }

    // Generic exception fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {

        log.info("GlobalExceptionHandler : Exception caught");

        // Print stack trace to debug unexpected issues
        ex.printStackTrace();

        // Hide technical details to the API interface
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "errorCode", "INTERNAL_ERROR",
                "message", "Something went wrong. Please try again later."
        ));
    }
}
