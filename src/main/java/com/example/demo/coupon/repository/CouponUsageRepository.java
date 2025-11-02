package com.example.demo.coupon.repository;

import com.example.demo.coupon.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    
    List<CouponUsage> findByCouponIdAndUserId(Long couponId, Long userId);
    
    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.userId = :userId")
    Long countByCouponIdAndUserId(@Param("couponId") Long couponId, @Param("userId") Long userId);
    
    boolean existsByCouponIdAndUserIdAndCartId(Long couponId, Long userId, String cartId);
}

