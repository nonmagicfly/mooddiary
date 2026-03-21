package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationAppException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationApp(HttpServletRequest request, ValidationAppException ex) {
        return build(request, HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(ConflictAppException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(HttpServletRequest request, ConflictAppException ex) {
        return build(request, HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(NotFoundAppException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(HttpServletRequest request, NotFoundAppException ex) {
        return build(request, HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(e -> e.getDefaultMessage() == null ? e.toString() : e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(request, HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(HttpServletRequest request, DataIntegrityViolationException ex) {
        return build(request, HttpStatus.BAD_REQUEST, "Bad Request", "Invalid request data");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(HttpServletRequest request, IllegalArgumentException ex) {
        return build(request, HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(HttpServletRequest request, IllegalStateException ex) {
        return build(request, HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandled(HttpServletRequest request, Exception ex) {
        return build(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error");
    }

    private ResponseEntity<ApiErrorResponse> build(HttpServletRequest request, HttpStatus status, String error, String message) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}

