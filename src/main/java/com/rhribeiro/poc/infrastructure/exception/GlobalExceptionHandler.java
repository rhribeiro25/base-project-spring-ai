package com.rhribeiro.poc.infrastructure.exception;

import com.rhribeiro.poc.application.exception.AiProviderException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        var details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).sorted().toList();
        return error(HttpStatus.BAD_REQUEST, "Invalid request", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> malformedJson(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid JSON request", List.of());
    }

    @ExceptionHandler(AiProviderException.class)
    ResponseEntity<ApiError> provider(AiProviderException exception) {
        return error(HttpStatus.BAD_GATEWAY, "AI provider unavailable or returned an invalid response", List.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), message, details));
    }
}
