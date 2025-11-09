package ru.yandex.practicum.exception;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleFeignException(final FeignException e) {
        return ResponseEntity.status(e.status())
                .body(Map.of("type", e.getClass().getName(),
                        "message", e.getMessage(),
                        "cause", e.getCause().toString()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(final ConstraintViolationException e) {
        boolean isUsernameViolation = e.getConstraintViolations().stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("username"));

        if (isUsernameViolation) {
            throw new NotAuthorizedUserException(401, e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("type", e.getClass().getName(),
                        "message", e.getMessage(),
                        "cause", e.getCause().toString()));
    }
}