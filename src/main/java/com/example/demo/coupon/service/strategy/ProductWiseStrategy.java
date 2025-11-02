package com.example.demo.coupon.service.strategy;

import com.example.demo.coupon.dto.ApplicableCouponResponse;
import com.example.demo.coupon.dto.CartItemRequest;
import com.example.demo.coupon.dto.CartItemResponse;
import com.example.demo.coupon.dto.DiscountBreakdown;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.ProductWiseCoupon;
import com.example.demo.coupon.model.enums.DiscountType;
import com.example.demo.coupon.util.DiscountCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductWiseStrategy implements CouponStrategy {
    
    @Override
    public boolean isApplicable(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!(coupon instanceof ProductWiseCoupon productWiseCoupon)) {
            return false;
        }
        
        Set<Long> applicableProductIds = productWiseCoupon.getApplicableProductIds();
        if (applicableProductIds == null || applicableProductIds.isEmpty()) {
            return false;
        }
        
        // Check if at least one applicable product is in cart
        boolean hasApplicableProduct = cartItems.stream()
            .anyMatch(item -> applicableProductIds.contains(item.getProductId()));
        
        if (!hasApplicableProduct) {
            return false;
        }
        
        // Check minimum quantity requirement if specified
        if (productWiseCoupon.getMinQuantity() != null) {
            Map<Long, Integer> productQuantities = cartItems.stream()
                .filter(item -> applicableProductIds.contains(item.getProductId()))
                .collect(Collectors.toMap(
                    CartItemRequest::getProductId,
                    CartItemRequest::getQuantity,
                    Integer::sum));
            
            // Check if any applicable product meets minimum quantity
            return productQuantities.values().stream()
                .anyMatch(qty -> qty >= productWiseCoupon.getMinQuantity());
        }
        
        return true;
    }
    
    @Override
    public BigDecimal calculateDiscount(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!isApplicable(coupon, cartItems, cartTotal)) {
            return BigDecimal.ZERO;
        }
        
        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        Set<Long> applicableProductIds = productWiseCoupon.getApplicableProductIds();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (CartItemRequest item : cartItems) {
            if (!applicableProductIds.contains(item.getProductId())) {
                continue;
            }
            
            // Check minimum quantity
            if (productWiseCoupon.getMinQuantity() != null && 
                item.getQuantity() < productWiseCoupon.getMinQuantity()) {
                continue;
            }
            
            // Determine quantity to apply discount to
            int quantityForDiscount = item.getQuantity();
            if (productWiseCoupon.getMaxQuantity() != null) {
                quantityForDiscount = Math.min(quantityForDiscount, productWiseCoupon.getMaxQuantity());
            }
            
            BigDecimal productTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(quantityForDiscount));
            
            BigDecimal itemDiscount = BigDecimal.ZERO;
            if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                itemDiscount = DiscountCalculator.calculatePercentageDiscount(
                    productTotal, coupon.getDiscountValue());
            } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                itemDiscount = DiscountCalculator.calculateFixedDiscount(
                    productTotal, coupon.getDiscountValue());
            }
            
            totalDiscount = totalDiscount.add(itemDiscount);
        }
        
        return DiscountCalculator.round(totalDiscount);
    }
    
    @Override
    public ApplicableCouponResponse getApplicabilityInfo(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        boolean applicable = isApplicable(coupon, cartItems, cartTotal);
        BigDecimal discount = applicable ? calculateDiscount(coupon, cartItems, cartTotal) : BigDecimal.ZERO;
        
        Map<Long, BigDecimal> productDiscounts = new HashMap<>();
        if (applicable) {
            ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
            Set<Long> applicableProductIds = productWiseCoupon.getApplicableProductIds();
            
            for (CartItemRequest item : cartItems) {
                if (applicableProductIds.contains(item.getProductId())) {
                    int quantityForDiscount = item.getQuantity();
                    if (productWiseCoupon.getMaxQuantity() != null) {
                        quantityForDiscount = Math.min(quantityForDiscount, productWiseCoupon.getMaxQuantity());
                    }
                    
                    BigDecimal productTotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(quantityForDiscount));
                    
                    BigDecimal itemDiscount = BigDecimal.ZERO;
                    if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                        itemDiscount = DiscountCalculator.calculatePercentageDiscount(
                            productTotal, coupon.getDiscountValue());
                    } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                        itemDiscount = DiscountCalculator.calculateFixedDiscount(
                            productTotal, coupon.getDiscountValue());
                    }
                    
                    productDiscounts.put(item.getProductId(), DiscountCalculator.round(itemDiscount));
                }
            }
        }
        
        DiscountBreakdown breakdown = DiscountBreakdown.builder()
            .cartTotal(cartTotal)
            .discountAmount(discount)
            .productDiscounts(productDiscounts)
            .build();
        
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            breakdown.setDiscountPercentage(coupon.getDiscountValue());
        }
        
        String message = applicable 
            ? "Coupon applicable to eligible products" 
            : "No applicable products found in cart or minimum quantity requirement not met";
        
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
        ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
        Set<Long> applicableProductIds = productWiseCoupon.getApplicableProductIds();
        
        return cartItems.stream()
            .map(item -> {
                BigDecimal originalPrice = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                
                // Check if product is applicable
                if (!applicableProductIds.contains(item.getProductId())) {
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
                
                // Check minimum quantity
                if (productWiseCoupon.getMinQuantity() != null && 
                    item.getQuantity() < productWiseCoupon.getMinQuantity()) {
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
                
                // Determine quantity to apply discount to
                int quantityForDiscount = item.getQuantity();
                if (productWiseCoupon.getMaxQuantity() != null) {
                    quantityForDiscount = Math.min(quantityForDiscount, productWiseCoupon.getMaxQuantity());
                }
                
                BigDecimal discountedTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(quantityForDiscount));
                
                BigDecimal itemDiscount = BigDecimal.ZERO;
                if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                    itemDiscount = DiscountCalculator.calculatePercentageDiscount(
                        discountedTotal, coupon.getDiscountValue());
                } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    itemDiscount = DiscountCalculator.calculateFixedDiscount(
                        discountedTotal, coupon.getDiscountValue());
                }
                
                BigDecimal discountedPrice = originalPrice.subtract(itemDiscount);
                
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
}

