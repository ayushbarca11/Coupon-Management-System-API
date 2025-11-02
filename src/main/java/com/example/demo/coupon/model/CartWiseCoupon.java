package com.example.demo.coupon.model;

import com.example.demo.coupon.model.enums.CouponType;
import com.example.demo.coupon.model.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CART_WISE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartWiseCoupon extends Coupon {
    
    @Column(name = "min_cart_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minCartAmount;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount; // Optional
    
    @PostPersist
    @PostUpdate
    private void setType() {
        if (super.getType() == null) {
            super.setType(CouponType.CART_WISE);
        }
    }
}

