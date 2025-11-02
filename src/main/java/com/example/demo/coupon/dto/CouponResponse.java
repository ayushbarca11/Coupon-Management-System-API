package com.example.demo.coupon.dto;

import com.example.demo.coupon.model.enums.BxGyDiscountType;
import com.example.demo.coupon.model.enums.CouponType;
import com.example.demo.coupon.model.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    
    private Long id;
    private String code;
    private String name;
    private CouponType type;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer currentUsage;
    private Integer maxUsagePerUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Cart-wise specific fields
    private BigDecimal minCartAmount;
    private BigDecimal maxDiscountAmount;
    
    // Product-wise specific fields
    private Set<Long> applicableProductIds;
    private Integer minQuantity;
    private Integer maxQuantity;
    
    // BxGy specific fields
    private Set<Long> buyProductIds;
    private Integer buyQuantity;
    private Set<Long> getProductIds;
    private Integer getQuantity;
    private Integer repetitionLimit;
    private BxGyDiscountType bxGyDiscountType;
}

