package com.example.demo.coupon.exception;

public class DuplicateCouponCodeException extends RuntimeException {
    
    public DuplicateCouponCodeException(String code) {
        super("Coupon code already exists: " + code);
    }
}

