package ru.yandex.practicum.exception;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleFeignException(final FeignException e) {
        Map<String, String> responseBody = Map.of(
                "type", e.getClass().getSimpleName(),
                "message", e.getMessage(),
                "status", String.valueOf(e.status())
        );

        return ResponseEntity.status(e.status()).body(responseBody);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(final ConstraintViolationException e) {
        boolean isUsernameViolation = e.getConstraintViolations().stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("username"));

        if (isUsernameViolation) {
            throw new NotAuthorizedUserException(401, e.getMessage());
        }

        Map<String, String> responseBody = Map.of(
                "type", e.getClass().getSimpleName(),
                "message", e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleThrowable(final Throwable e) {
        Map<String, String> responseBody = Map.of(
                "type", e.getClass().getSimpleName(),
                "message", e.getMessage() != null ? e.getMessage() : "Internal server error"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}