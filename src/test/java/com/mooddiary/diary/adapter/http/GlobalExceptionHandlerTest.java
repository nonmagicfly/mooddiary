package com.mooddiary.diary.adapter.http;

import com.mooddiary.diary.application.exception.ConflictAppException;
import com.mooddiary.diary.application.exception.NotFoundAppException;
import com.mooddiary.diary.application.exception.ValidationAppException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    @Test
    void shouldMapValidationExceptionTo400() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/diary-entries");

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationApp(request, new ValidationAppException("bad"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("bad", response.getBody().message());
        assertEquals("/api/v1/diary-entries", response.getBody().path());
        assertEquals("Bad Request", response.getBody().error());
    }

    @Test
    void shouldMapConflictExceptionTo409() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/diary-entries");

        ResponseEntity<ApiErrorResponse> response = handler.handleConflict(request, new ConflictAppException("conflict"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("conflict", response.getBody().message());
    }

    @Test
    void shouldMapNotFoundExceptionTo404() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/diary-entries/1");

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(request, new NotFoundAppException("not found"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("not found", response.getBody().message());
        assertEquals("/api/v1/diary-entries/1", response.getBody().path());
    }
}

