package com.example.demo.coupon.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiscountCalculator {
    
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Calculate percentage discount
     */
    public static BigDecimal calculatePercentageDiscount(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percentage)
                     .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate fixed amount discount (ensures it doesn't exceed the amount)
     */
    public static BigDecimal calculateFixedDiscount(BigDecimal amount, BigDecimal fixedDiscount) {
        if (amount == null || fixedDiscount == null) {
            return BigDecimal.ZERO;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return fixedDiscount.min(amount);
    }
    
    /**
     * Apply maximum discount cap
     */
    public static BigDecimal applyDiscountCap(BigDecimal discount, BigDecimal maxDiscount) {
        if (maxDiscount == null) {
            return discount;
        }
        return discount.min(maxDiscount);
    }
    
    /**
     * Ensure discount doesn't make total negative
     */
    public static BigDecimal ensureNonNegative(BigDecimal total, BigDecimal discount) {
        if (total == null || discount == null) {
            return total != null ? total : BigDecimal.ZERO;
        }
        BigDecimal result = total.subtract(discount);
        return result.max(BigDecimal.ZERO);
    }
    
    /**
     * Round to 2 decimal places
     */
    public static BigDecimal round(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(SCALE, ROUNDING_MODE);
    }
}

