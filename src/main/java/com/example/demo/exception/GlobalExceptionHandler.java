package com.example.demo.exception;

import com.example.demo.coupon.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCouponNotFoundException(CouponNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Coupon not found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCouponException(InvalidCouponException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid coupon");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(DuplicateCouponCodeException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateCouponCodeException(DuplicateCouponCodeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Duplicate coupon code");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(CouponNotApplicableException.class)
    public ResponseEntity<Map<String, String>> handleCouponNotApplicableException(CouponNotApplicableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Coupon not applicable");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("error", "Validation failed");
        errors.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal server error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
