package com.example.demo.coupon.validator;

import com.example.demo.coupon.exception.InvalidCouponException;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.CartWiseCoupon;
import com.example.demo.coupon.model.ProductWiseCoupon;
import com.example.demo.coupon.model.BxGyCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CouponValidator {
    
    /**
     * Validate coupon is applicable (dates, active status, usage limits)
     */
    public void validateCouponApplicable(Coupon coupon, Long userId, 
                                         Long userUsageCount) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if coupon is active
        if (!coupon.getIsActive() || coupon.getIsActive() == null) {
            throw new InvalidCouponException("Coupon is not active");
        }
        
        // Check date range
        if (now.isBefore(coupon.getStartDate())) {
            throw new InvalidCouponException("Coupon is not yet active. Valid from: " + coupon.getStartDate());
        }
        
        if (now.isAfter(coupon.getEndDate())) {
            throw new InvalidCouponException("Coupon has expired. Valid until: " + coupon.getEndDate());
        }
        
        // Check global usage limit
        if (coupon.getMaxUsage() != null && coupon.getCurrentUsage() >= coupon.getMaxUsage()) {
            throw new InvalidCouponException("Coupon usage limit exceeded");
        }
        
        // Check per-user usage limit
        if (userId != null && coupon.getMaxUsagePerUser() != null && 
            userUsageCount != null && userUsageCount >= coupon.getMaxUsagePerUser()) {
            throw new InvalidCouponException("User has exceeded the usage limit for this coupon");
        }
    }
    
    /**
     * Validate cart-wise coupon requirements
     */
    public void validateCartWiseCoupon(CartWiseCoupon coupon, BigDecimal cartTotal) {
        if (coupon.getMinCartAmount() == null) {
            throw new InvalidCouponException("Minimum cart amount is required for cart-wise coupon");
        }
        
        if (cartTotal == null || cartTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCouponException("Cart total must be greater than zero");
        }
        
        if (cartTotal.compareTo(coupon.getMinCartAmount()) < 0) {
            throw new InvalidCouponException(
                String.format("Cart total (%.2f) is less than minimum required (%.2f)", 
                    cartTotal, coupon.getMinCartAmount()));
        }
    }
    
    /**
     * Validate product-wise coupon requirements
     */
    public void validateProductWiseCoupon(ProductWiseCoupon coupon) {
        if (coupon.getApplicableProductIds() == null || 
            coupon.getApplicableProductIds().isEmpty()) {
            throw new InvalidCouponException("Product-wise coupon must have at least one applicable product");
        }
        
        if (coupon.getMinQuantity() != null && coupon.getMinQuantity() <= 0) {
            throw new InvalidCouponException("Minimum quantity must be greater than zero");
        }
        
        if (coupon.getMaxQuantity() != null && coupon.getMinQuantity() != null &&
            coupon.getMaxQuantity() < coupon.getMinQuantity()) {
            throw new InvalidCouponException("Maximum quantity cannot be less than minimum quantity");
        }
    }
    
    /**
     * Validate BxGy coupon requirements
     */
    public void validateBxGyCoupon(BxGyCoupon coupon) {
        if (coupon.getBuyProductIds() == null || coupon.getBuyProductIds().isEmpty()) {
            throw new InvalidCouponException("BxGy coupon must have at least one buy product");
        }
        
        if (coupon.getGetProductIds() == null || coupon.getGetProductIds().isEmpty()) {
            throw new InvalidCouponException("BxGy coupon must have at least one get product");
        }
        
        if (coupon.getBuyQuantity() == null || coupon.getBuyQuantity() <= 0) {
            throw new InvalidCouponException("Buy quantity must be greater than zero");
        }
        
        if (coupon.getGetQuantity() == null || coupon.getGetQuantity() <= 0) {
            throw new InvalidCouponException("Get quantity must be greater than zero");
        }
        
        if (coupon.getRepetitionLimit() == null || coupon.getRepetitionLimit() <= 0) {
            throw new InvalidCouponException("Repetition limit must be greater than zero");
        }
    }
    
    /**
     * Validate generic coupon fields
     */
    public void validateCouponFields(Coupon coupon) {
        if (coupon.getDiscountValue() == null || 
            coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCouponException("Discount value must be greater than zero");
        }
        
        if (coupon.getStartDate() == null || coupon.getEndDate() == null) {
            throw new InvalidCouponException("Start date and end date are required");
        }
        
        if (coupon.getStartDate().isAfter(coupon.getEndDate())) {
            throw new InvalidCouponException("Start date cannot be after end date");
        }
        
        // Validate discount value limits
        if (coupon.getDiscountType().name().equals("PERCENTAGE") && 
            coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidCouponException("Percentage discount cannot exceed 100%");
        }
    }
}

