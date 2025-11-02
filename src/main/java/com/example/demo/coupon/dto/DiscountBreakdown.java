package com.example.demo.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountBreakdown {
    
    private BigDecimal cartTotal;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private Map<Long, BigDecimal> productDiscounts; // For product-wise discounts
    private Integer buyQuantity; // For BxGy
    private Integer getQuantity; // For BxGy
    private Integer applications; // For BxGy - how many times deal applied
}

