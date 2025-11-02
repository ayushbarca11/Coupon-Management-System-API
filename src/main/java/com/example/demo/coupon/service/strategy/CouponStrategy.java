package com.example.demo.coupon.service.strategy;

import com.example.demo.coupon.dto.ApplicableCouponResponse;
import com.example.demo.coupon.dto.CartItemRequest;
import com.example.demo.coupon.dto.CartItemResponse;
import com.example.demo.coupon.model.Coupon;

import java.math.BigDecimal;
import java.util.List;

public interface CouponStrategy {
    
    /**
     * Check if coupon is applicable to the cart
     */
    boolean isApplicable(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal);
    
    /**
     * Calculate discount amount for the coupon (without applying)
     */
    BigDecimal calculateDiscount(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal);
    
    /**
     * Get detailed applicability information
     */
    ApplicableCouponResponse getApplicabilityInfo(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal);
    
    /**
     * Apply coupon and return updated cart items with discounts
     */
    List<CartItemResponse> applyCoupon(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal);
    
    /**
     * Get total discount amount after applying
     */
    BigDecimal getTotalDiscountApplied(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal);
}

