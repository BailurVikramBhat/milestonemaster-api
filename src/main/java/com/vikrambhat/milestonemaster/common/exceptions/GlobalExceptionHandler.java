package com.vikrambhat.milestonemaster.common.exceptions;

import com.vikrambhat.milestonemaster.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.debug("Authentication failed for {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        request.getRequestURI()
                ));
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        log.debug("Bad credentials for {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid email or password",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgsNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> failures = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String fieldMessage = fieldError.getDefaultMessage();
            failures.put(fieldName, fieldMessage);
        });
        log.debug("Validation failed for {} {} fields={}", request.getMethod(), request.getRequestURI(), failures.keySet());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse.validationErrors(HttpStatus.BAD_REQUEST.value(), "Bad Request", request.getRequestURI(), failures));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        request.getRequestURI()
                ));
    }

}
