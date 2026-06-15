package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class) // handle database constraint violations (e.g. unique constraint violations) and return a structured error response with a 409 Conflict status code, allowing clients to understand that the request failed due to a conflict with existing data when they access the relevant endpoint in the application
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(); // create a new ApiError object to represent the error response, populating it with the current timestamp, a 409 Conflict status code, a generic error message indicating that a record with the same unique value already exists, and the path of the request that caused the error, then return this structured error response to the client when they access the relevant endpoint in the application
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.CONFLICT.value()); // Set the HTTP status code to 409 Conflict to indicate that the request failed due to a conflict with existing data (e.g. a unique constraint violation)
        error.setError(HttpStatus.CONFLICT.getReasonPhrase());
        error.setMessage("A record with the same unique value already exists.");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // Return the structured error response with a 409 Conflict status code to indicate that the request failed due to a conflict with existing data (e.g. a unique constraint violation)
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // handle validation errors that occur when request data fails to meet the defined constraints (e.g. @NotNull, @Size) and return a structured error response with a 400 Bad Request status code, including details about which fields failed validation and the corresponding error messages, allowing clients to understand what went wrong with their request and how to fix it when they access the relevant endpoint in the application
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        ApiError error = new ApiError(); // create a new ApiError object to represent the error response, populating it with the current timestamp, a 400 Bad Request status code, a generic error message indicating that validation failed, the path of the request that caused the error, and a map of field errors where each key is the name of a field that failed validation and the corresponding value is the validation error message for that field, then return this structured error response to the client when they access the relevant endpoint in the application
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setMessage("Validation failed.");
        error.setPath(request.getRequestURI());
        error.setFieldErrors(fieldErrors);

        return ResponseEntity.badRequest().body(error);
    }


    @ExceptionHandler(ResponseStatusException.class) // handle exceptions of type ResponseStatusException, which are commonly used to indicate specific HTTP error conditions (e.g. 404 Not Found, 400 Bad Request), and return a structured error response with the appropriate status code and message based on the details of the exception, allowing clients to understand the specific reason for the failure when they access the relevant endpoint in the application
    public ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ApiError error = new ApiError();
        error.setTimestamp(Instant.now());
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setMessage(ex.getReason() != null ? ex.getReason() : "Request failed.");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler(Exception.class) // handle any unexpected exceptions that are not specifically handled by other exception handlers, returning a structured error response with a 500 Internal Server Error status code and a generic error message indicating that an unexpected error occurred, allowing clients to understand that the request failed due to an unforeseen issue when they access the relevant endpoint in the application
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        ApiError error = new ApiError();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMessage("An unexpected error occurred.");
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

