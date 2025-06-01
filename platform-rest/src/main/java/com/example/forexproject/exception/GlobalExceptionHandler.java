package com.example.forexproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler to standardize error responses in JSON.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Tüm exception türlerini yakalayıp standart JSON hata cevabı döner.
     *
     * @param ex      Fırlatılan exception.
     * @param request İstek bilgisi.
     * @return Hata detaylarını içeren ResponseEntity.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
