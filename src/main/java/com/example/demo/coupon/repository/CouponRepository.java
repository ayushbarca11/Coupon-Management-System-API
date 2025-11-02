package com.example.demo.coupon.repository;

import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.enums.CouponType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<Coupon> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Coupon> findByType(CouponType type, Pageable pageable);
    
    Page<Coupon> findByIsActiveAndType(Boolean isActive, CouponType type, Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.maxUsage IS NULL OR c.currentUsage < c.maxUsage)")
    List<Coupon> findApplicableCoupons(LocalDateTime now);
}

