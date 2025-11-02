package com.example.demo.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicableCouponsResponse {
    
    private List<ApplicableCouponResponse> applicableCoupons;
    private BigDecimal cartTotal;
    private BigDecimal bestDiscount; // Highest discount available
}

