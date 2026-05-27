package com.oms.order_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Handle Input Validation Errors (from @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request payload");
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create("https://api.oms.com/errors/validation"));
        problemDetail.setProperty("timestamp", Instant.now());

        // Extract specific field errors
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("invalid_fields", errors);

        log.warn("Validation error occurred: {}", errors);
        return problemDetail;
    }

    // 2. Handle Business Logic Errors (e.g., Order Not Found)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setType(URI.create("https://api.oms.com/errors/bad-request"));
        problemDetail.setProperty("timestamp", Instant.now());

        log.error("Illegal argument: {}", ex.getMessage());
        return problemDetail;
    }
    // Catch Circuit Breaker fallbacks and system state errors
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problemDetail.setTitle("Service Temporarily Unavailable");
        problemDetail.setType(URI.create("https://api.oms.com/errors/service-unavailable"));
        problemDetail.setProperty("timestamp", Instant.now());

        log.warn("Circuit breaker or state error: {}", ex.getMessage());
        return problemDetail;
    }

    // 3. Fallback for all other unexpected server crashes
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.oms.com/errors/server-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        log.error("Unhandled exception caught", ex);
        return problemDetail;
    }
}