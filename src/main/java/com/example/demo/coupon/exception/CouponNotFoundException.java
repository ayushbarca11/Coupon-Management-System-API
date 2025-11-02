package com.example.demo.coupon.exception;

public class CouponNotFoundException extends RuntimeException {
    
    public CouponNotFoundException(String message) {
        super(message);
    }
    
    public CouponNotFoundException(Long couponId) {
        super("Coupon not found with ID: " + couponId);
    }
}

