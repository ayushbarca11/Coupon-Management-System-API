package com.example.demo.coupon.service;

import com.example.demo.coupon.dto.*;
import com.example.demo.coupon.exception.CouponNotApplicableException;
import com.example.demo.coupon.exception.CouponNotFoundException;
import com.example.demo.coupon.exception.InvalidCouponException;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.CouponUsage;
import com.example.demo.coupon.repository.CouponRepository;
import com.example.demo.coupon.repository.CouponUsageRepository;
import com.example.demo.coupon.service.strategy.CouponStrategy;
import com.example.demo.coupon.service.strategy.CouponStrategyFactory;
import com.example.demo.coupon.util.DiscountCalculator;
import com.example.demo.coupon.validator.CartValidator;
import com.example.demo.coupon.validator.CouponValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponApplicationService {
    
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponStrategyFactory strategyFactory;
    private final CouponValidator couponValidator;
    private final CartValidator cartValidator;
    
    public ApplicableCouponsResponse getApplicableCoupons(CartRequest cartRequest) {
        log.info("Getting applicable coupons for cart with {} items", cartRequest.getCartItems().size());
        
        // Validate cart
        cartValidator.validateCartItems(cartRequest.getCartItems());
        
        // Calculate cart total
        BigDecimal cartTotal = cartValidator.calculateCartTotal(cartRequest.getCartItems());
        
        // Get all active and applicable coupons
        List<Coupon> allCoupons = couponRepository.findApplicableCoupons(LocalDateTime.now());
        
        List<ApplicableCouponResponse> applicableCoupons = new ArrayList<>();
        BigDecimal bestDiscount = BigDecimal.ZERO;
        
        for (Coupon coupon : allCoupons) {
            try {
                // Check per-user usage limit if userId provided
                if (cartRequest.getUserId() != null && coupon.getMaxUsagePerUser() != null) {
                    Long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(
                        coupon.getId(), cartRequest.getUserId());
                    if (userUsageCount >= coupon.getMaxUsagePerUser()) {
                        continue; // Skip this coupon
                    }
                }
                
                CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
                ApplicableCouponResponse response = strategy.getApplicabilityInfo(
                    coupon, cartRequest.getCartItems(), cartTotal);
                
                if (response.getIsApplicable()) {
                    applicableCoupons.add(response);
                    if (response.getTotalDiscount().compareTo(bestDiscount) > 0) {
                        bestDiscount = response.getTotalDiscount();
                    }
                }
            } catch (Exception e) {
                log.warn("Error checking coupon {} applicability: {}", coupon.getCode(), e.getMessage());
                // Continue with other coupons
            }
        }
        
        return ApplicableCouponsResponse.builder()
            .applicableCoupons(applicableCoupons)
            .cartTotal(cartTotal)
            .bestDiscount(bestDiscount)
            .build();
    }
    
    @Transactional
    public CartResponse applyCoupon(Long couponId, CartRequest cartRequest) {
        log.info("Applying coupon {} to cart", couponId);
        
        // Validate cart
        cartValidator.validateCartItems(cartRequest.getCartItems());
        
        // Get coupon
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException(couponId));
        
        // Check per-user usage
        Long userUsageCount = null;
        if (cartRequest.getUserId() != null && coupon.getMaxUsagePerUser() != null) {
            userUsageCount = couponUsageRepository.countByCouponIdAndUserId(
                coupon.getId(), cartRequest.getUserId());
        }
        
        // Validate coupon is applicable
        couponValidator.validateCouponApplicable(coupon, cartRequest.getUserId(), userUsageCount);
        
        // Calculate cart total
        BigDecimal cartTotal = cartValidator.calculateCartTotal(cartRequest.getCartItems());
        
        // Get strategy
        CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
        
        // Check if coupon is applicable to this cart
        if (!strategy.isApplicable(coupon, cartRequest.getCartItems(), cartTotal)) {
            throw new CouponNotApplicableException("Coupon is not applicable to this cart");
        }
        
        // Type-specific validation
        if (coupon instanceof com.example.demo.coupon.model.CartWiseCoupon cartWiseCoupon) {
            couponValidator.validateCartWiseCoupon(cartWiseCoupon, cartTotal);
        }
        
        // Apply coupon
        List<CartItemResponse> updatedCartItems = strategy.applyCoupon(
            coupon, cartRequest.getCartItems(), cartTotal);
        
        // Calculate totals
        BigDecimal originalTotal = cartTotal;
        BigDecimal discountApplied = strategy.getTotalDiscountApplied(
            coupon, cartRequest.getCartItems(), cartTotal);
        BigDecimal finalTotal = DiscountCalculator.ensureNonNegative(originalTotal, discountApplied);
        
        // Update coupon usage
        coupon.setCurrentUsage(coupon.getCurrentUsage() + 1);
        couponRepository.save(coupon);
        
        // Record coupon usage
        String cartId = "cart-" + System.currentTimeMillis() + "-" + 
                       (cartRequest.getUserId() != null ? cartRequest.getUserId() : "anonymous");
        
        CouponUsage usage = CouponUsage.builder()
            .coupon(coupon)
            .userId(cartRequest.getUserId())
            .cartId(cartId)
            .discountAmount(discountApplied)
            .usedAt(LocalDateTime.now())
            .build();
        couponUsageRepository.save(usage);
        
        log.info("Coupon applied successfully. Discount: {}, Final Total: {}", discountApplied, finalTotal);
        
        // Build response
        CartResponse.AppliedCouponInfo appliedCouponInfo = CartResponse.AppliedCouponInfo.builder()
            .couponId(coupon.getId())
            .couponCode(coupon.getCode())
            .couponType(coupon.getType())
            .build();
        
        return CartResponse.builder()
            .cartId(cartId)
            .originalTotal(DiscountCalculator.round(originalTotal))
            .discountApplied(DiscountCalculator.round(discountApplied))
            .finalTotal(DiscountCalculator.round(finalTotal))
            .appliedCoupon(appliedCouponInfo)
            .cartItems(updatedCartItems)
            .build();
    }
}

