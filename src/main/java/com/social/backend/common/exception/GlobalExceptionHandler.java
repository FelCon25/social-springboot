package com.social.backend.common.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
                log.warn("Business exception [{}]:, path {}", ex.getErrorCode(), ex.getMessage());
                ApiError apiError = ApiError.of(
                                ex.getStatus().value(),
                                ex.getErrorCode(),
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(ex.getStatus()).body(apiError);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> new ApiError.FieldError(fe.getField(),
                                                fe.getDefaultMessage() == null ? "invalid value"
                                                                : fe.getDefaultMessage()))
                                .toList();
                ApiError body = ApiError.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "VALIDATION_FAILED",
                                "Request validation failed",
                                fieldErrors,
                                request.getRequestURI());
                return ResponseEntity.badRequest().body(body);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                log.error("Request body missing or malformed: {}", ex.getMessage());
                ApiError body = ApiError.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "MALFORMED_REQUEST",
                                "Request body is missing or malformed",
                                request.getRequestURI());
                return ResponseEntity.badRequest().body(body);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {
                log.warn("Method not supported: {}", ex.getMessage());
                ApiError body = ApiError.of(
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                "METHOD_NOT_ALLOWED",
                                "The requested HTTP method is not supported for this endpoint",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                        HttpServletRequest request) {
                log.warn("Media type not supported: {}", ex.getMessage());
                ApiError body = ApiError.of(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                                "UNSUPPORTED_MEDIA_TYPE",
                                "The Content-Type of the request is not supported. Please use application/json.",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
                log.error("Unhandled exception", ex);
                ApiError body = ApiError.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "INTERNAL_ERROR",
                                "An unexpected error occurred",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

}
