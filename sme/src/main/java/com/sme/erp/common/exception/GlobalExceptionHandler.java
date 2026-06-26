package com.sme.erp.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Environment environment;

    public GlobalExceptionHandler() {
        this(new StandardEnvironment());
    }

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                validationErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                resolveMeaningfulMessage(ex, "Request body format is invalid"),
                request,
                null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                resolveMeaningfulMessage(ex, "Database constraint violation"),
                request,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                isProdProfile() ? "An unexpected error occurred" : resolveMeaningfulMessage(ex, "An unexpected error occurred"),
                request,
                null);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> errors) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }

        return ResponseEntity.status(status).body(body);
    }

    private String resolveMeaningfulMessage(Throwable throwable, String fallback) {
        Throwable current = throwable;
        String bestMessage = null;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.trim();
                if (!normalized.isEmpty()
                        && !"n/a".equalsIgnoreCase(normalized)
                        && !normalized.equals(current.getClass().getName())) {
                    bestMessage = normalized;
                    if (!isGenericPersistenceMessage(normalized)) {
                        return normalized;
                    }
                }
            }
            current = current.getCause();
        }
        return bestMessage != null ? bestMessage : fallback;
    }

    private boolean isGenericPersistenceMessage(String message) {
        String normalized = message.toLowerCase();
        return normalized.equals("could not execute statement")
                || normalized.equals("jdbc exception executing sql")
                || normalized.equals("request body format is invalid")
                || normalized.equals("database constraint violation")
                || normalized.equals("an unexpected error occurred");
    }

    private boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
