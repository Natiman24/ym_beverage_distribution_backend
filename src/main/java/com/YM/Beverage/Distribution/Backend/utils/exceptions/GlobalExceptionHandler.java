package com.YM.Beverage.Distribution.Backend.utils.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String , Object>> handleUnAuthorization(Exception ex) {
        Map<String , Object> response = new HashMap<>();
        response.put("message","you can't access this resource because of your role");
        response.put("code",HttpStatus.FORBIDDEN);
        response.put("exception",ex.getMessage());
        return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message",ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        errors.put("code", String.valueOf(HttpStatus.BAD_REQUEST));
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String , Object>> handleSecurityException(Exception ex) {
        Map<String , Object> response = new HashMap<>();
        response.put("message",ex.getMessage());
        response.put("code",HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String , Object>> handleDataNotFoundException(DataNotFoundException ex) {
        Map<String , Object> response = new HashMap<>();
        response.put("message",ex.getMessage());
        response.put("code",HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String , Object>> handleDataAlreadyExistsException(DataAlreadyExistsException ex) {
        Map<String , Object> response = new HashMap<>();
        response.put("message",ex.getMessage());
        response.put("code",HttpStatus.CONFLICT);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.warn("Database constraint violation", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "The submitted data conflicts with an existing record");
        response.put("code", HttpStatus.CONFLICT);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String , Object>> handleCustomException(CustomException ex) {
        Map<String , Object> response = new HashMap<>();
        response.put("message",ex.getMessage());
        response.put("code",ex.getStatusCode());
        response.put("exception",ex.getException());
        return new ResponseEntity<>(response,ex.getStatusCode());
    }

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String , Object>> handleGlobalException(Exception ex) {
        logger.error("An unexpected error occurred", ex);
        Map<String , Object> response = new HashMap<>();
        response.put("message","could not process your request");
        response.put("code",HttpStatus.INTERNAL_SERVER_ERROR);
        response.put("exception",ex.getMessage());
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
