package com.example.demo.coupon.service.strategy;

import com.example.demo.coupon.model.enums.CouponType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponStrategyFactory {
    
    private final CartWiseStrategy cartWiseStrategy;
    private final ProductWiseStrategy productWiseStrategy;
    private final BxGyStrategy bxGyStrategy;
    
    public CouponStrategy getStrategy(CouponType couponType) {
        return switch (couponType) {
            case CART_WISE -> cartWiseStrategy;
            case PRODUCT_WISE -> productWiseStrategy;
            case BXGY -> bxGyStrategy;
        };
    }
}

