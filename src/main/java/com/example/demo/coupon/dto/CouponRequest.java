package com.example.demo.coupon.dto;

import com.example.demo.coupon.model.enums.BxGyDiscountType;
import com.example.demo.coupon.model.enums.CouponType;
import com.example.demo.coupon.model.enums.DiscountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class CouponRequest {
    
    @NotBlank(message = "Coupon code is required")
    private String code;
    
    @NotBlank(message = "Coupon name is required")
    private String name;
    
    @NotNull(message = "Coupon type is required")
    private CouponType type;
    
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsage;
    
    @Min(value = 1, message = "Max usage per user must be at least 1")
    private Integer maxUsagePerUser;
    
    // Cart-wise specific fields
    private BigDecimal minCartAmount;
    private BigDecimal maxDiscountAmount;
    
    // Product-wise specific fields
    private Set<Long> applicableProductIds;
    @Min(value = 1, message = "Min quantity must be at least 1")
    private Integer minQuantity;
    @Min(value = 1, message = "Max quantity must be at least 1")
    private Integer maxQuantity;
    
    // BxGy specific fields
    private Set<Long> buyProductIds;
    @Min(value = 1, message = "Buy quantity must be at least 1")
    private Integer buyQuantity;
    private Set<Long> getProductIds;
    @Min(value = 1, message = "Get quantity must be at least 1")
    private Integer getQuantity;
    @Min(value = 1, message = "Repetition limit must be at least 1")
    private Integer repetitionLimit;
    private BxGyDiscountType bxGyDiscountType;
}

