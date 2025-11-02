package com.example.demo.coupon.model;

import com.example.demo.coupon.model.enums.BxGyDiscountType;
import com.example.demo.coupon.model.enums.CouponType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("BXGY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BxGyCoupon extends Coupon {
    
    @ElementCollection
    @CollectionTable(name = "bxgy_buy_products",
                     joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private Set<Long> buyProductIds = new HashSet<>();
    
    @Column(name = "buy_quantity", nullable = false)
    private Integer buyQuantity;
    
    @ElementCollection
    @CollectionTable(name = "bxgy_get_products",
                     joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private Set<Long> getProductIds = new HashSet<>();
    
    @Column(name = "get_quantity", nullable = false)
    private Integer getQuantity;
    
    @Column(name = "repetition_limit", nullable = false)
    private Integer repetitionLimit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bxgy_discount_type", nullable = false)
    private BxGyDiscountType bxGyDiscountType;
    
    @PostPersist
    @PostUpdate
    private void setType() {
        if (super.getType() == null) {
            super.setType(CouponType.BXGY);
        }
    }
}

