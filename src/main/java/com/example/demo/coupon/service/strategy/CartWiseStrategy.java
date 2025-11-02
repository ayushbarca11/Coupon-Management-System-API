package com.example.demo.coupon.service.strategy;

import com.example.demo.coupon.dto.ApplicableCouponResponse;
import com.example.demo.coupon.dto.CartItemRequest;
import com.example.demo.coupon.dto.CartItemResponse;
import com.example.demo.coupon.dto.DiscountBreakdown;
import com.example.demo.coupon.model.CartWiseCoupon;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.enums.DiscountType;
import com.example.demo.coupon.util.DiscountCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CartWiseStrategy implements CouponStrategy {
    
    @Override
    public boolean isApplicable(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!(coupon instanceof CartWiseCoupon cartWiseCoupon)) {
            return false;
        }
        
        if (cartTotal == null || cartTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        BigDecimal minCartAmount = cartWiseCoupon.getMinCartAmount();
        return minCartAmount != null && cartTotal.compareTo(minCartAmount) >= 0;
    }
    
    @Override
    public BigDecimal calculateDiscount(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!isApplicable(coupon, cartItems, cartTotal)) {
            return BigDecimal.ZERO;
        }
        
        CartWiseCoupon cartWiseCoupon = (CartWiseCoupon) coupon;
        BigDecimal discount = BigDecimal.ZERO;
        
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = DiscountCalculator.calculatePercentageDiscount(
                cartTotal, coupon.getDiscountValue());
            
            // Apply maximum discount cap if specified
            if (cartWiseCoupon.getMaxDiscountAmount() != null) {
                discount = DiscountCalculator.applyDiscountCap(
                    discount, cartWiseCoupon.getMaxDiscountAmount());
            }
        } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = DiscountCalculator.calculateFixedDiscount(
                cartTotal, coupon.getDiscountValue());
        }
        
        return DiscountCalculator.round(discount);
    }
    
    @Override
    public ApplicableCouponResponse getApplicabilityInfo(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        boolean applicable = isApplicable(coupon, cartItems, cartTotal);
        BigDecimal discount = applicable ? calculateDiscount(coupon, cartItems, cartTotal) : BigDecimal.ZERO;
        
        DiscountBreakdown breakdown = DiscountBreakdown.builder()
            .cartTotal(cartTotal)
            .discountAmount(discount)
            .build();
        
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            breakdown.setDiscountPercentage(coupon.getDiscountValue());
        }
        
        String message = applicable 
            ? "Coupon applicable" 
            : String.format("Cart total (%.2f) is less than minimum required (%.2f)", 
                cartTotal, ((CartWiseCoupon) coupon).getMinCartAmount());
        
        return ApplicableCouponResponse.builder()
            .couponId(coupon.getId())
            .couponCode(coupon.getCode())
            .couponName(coupon.getName())
            .couponType(coupon.getType())
            .totalDiscount(discount)
            .discountBreakdown(breakdown)
            .isApplicable(applicable)
            .applicabilityMessage(message)
            .build();
    }
    
    @Override
    public List<CartItemResponse> applyCoupon(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        BigDecimal totalDiscount = calculateDiscount(coupon, cartItems, cartTotal);
        
        if (totalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            // No discount, return items as-is
            return cartItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        }
        
        // Calculate proportional discount for each item
        BigDecimal finalCartTotal = cartTotal.subtract(totalDiscount);
        BigDecimal discountRatio = cartTotal.compareTo(BigDecimal.ZERO) > 0
            ? finalCartTotal.divide(cartTotal, 4, RoundingMode.HALF_UP)
            : BigDecimal.ONE;
        
        return cartItems.stream()
            .map(item -> {
                BigDecimal originalPrice = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal discountedPrice = originalPrice.multiply(discountRatio);
                BigDecimal itemDiscount = originalPrice.subtract(discountedPrice);
                
                return CartItemResponse.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .originalPrice(DiscountCalculator.round(originalPrice))
                    .discountedPrice(DiscountCalculator.round(discountedPrice))
                    .discountApplied(DiscountCalculator.round(itemDiscount))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public BigDecimal getTotalDiscountApplied(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        return calculateDiscount(coupon, cartItems, cartTotal);
    }
    
    private CartItemResponse mapToResponse(CartItemRequest item) {
        BigDecimal originalPrice = item.getUnitPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity()));
        
        return CartItemResponse.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .originalPrice(DiscountCalculator.round(originalPrice))
            .discountedPrice(DiscountCalculator.round(originalPrice))
            .discountApplied(BigDecimal.ZERO)
            .build();
    }
}

