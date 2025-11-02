package com.example.demo.coupon.dto;

import com.example.demo.coupon.model.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicableCouponResponse {
    
    private Long couponId;
    private String couponCode;
    private String couponName;
    private CouponType couponType;
    private BigDecimal totalDiscount;
    private DiscountBreakdown discountBreakdown;
    private Boolean isApplicable;
    private String applicabilityMessage;
}

