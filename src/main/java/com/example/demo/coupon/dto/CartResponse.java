package com.example.demo.coupon.dto;

import com.example.demo.coupon.model.enums.CouponType;
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
public class CartResponse {
    
    private String cartId;
    private BigDecimal originalTotal;
    private BigDecimal discountApplied;
    private BigDecimal finalTotal;
    private AppliedCouponInfo appliedCoupon;
    private List<CartItemResponse> cartItems;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedCouponInfo {
        private Long couponId;
        private String couponCode;
        private CouponType couponType;
    }
}

