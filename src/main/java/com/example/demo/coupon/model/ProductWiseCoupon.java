package com.example.demo.coupon.model;

import com.example.demo.coupon.model.enums.CouponType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("PRODUCT_WISE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductWiseCoupon extends Coupon {
    
    @ElementCollection
    @CollectionTable(name = "coupon_applicable_products", 
                     joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private Set<Long> applicableProductIds = new HashSet<>();
    
    @Column(name = "min_quantity")
    private Integer minQuantity; // Optional
    
    @Column(name = "max_quantity")
    private Integer maxQuantity; // Optional, null means unlimited
    
    @PostPersist
    @PostUpdate
    private void setType() {
        if (super.getType() == null) {
            super.setType(CouponType.PRODUCT_WISE);
        }
    }
}

