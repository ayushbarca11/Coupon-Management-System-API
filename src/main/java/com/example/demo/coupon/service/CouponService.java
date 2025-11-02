package com.example.demo.coupon.service;

import com.example.demo.coupon.dto.CouponRequest;
import com.example.demo.coupon.dto.CouponResponse;
import com.example.demo.coupon.exception.CouponNotFoundException;
import com.example.demo.coupon.exception.DuplicateCouponCodeException;
import com.example.demo.coupon.exception.InvalidCouponException;
import com.example.demo.coupon.model.*;
import com.example.demo.coupon.model.enums.CouponType;
import com.example.demo.coupon.repository.CouponRepository;
import com.example.demo.coupon.validator.CouponValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final CouponValidator couponValidator;
    
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        log.info("Creating coupon with code: {}", request.getCode());
        
        // Check for duplicate code
        if (couponRepository.existsByCode(request.getCode())) {
            throw new DuplicateCouponCodeException(request.getCode());
        }
        
        // Create coupon based on type
        Coupon coupon = createCouponFromRequest(request);
        
        // Validate coupon
        couponValidator.validateCouponFields(coupon);
        
        // Type-specific validation
        if (coupon instanceof CartWiseCoupon) {
            couponValidator.validateCartWiseCoupon((CartWiseCoupon) coupon, request.getMinCartAmount());
        } else if (coupon instanceof ProductWiseCoupon) {
            couponValidator.validateProductWiseCoupon((ProductWiseCoupon) coupon);
        } else if (coupon instanceof BxGyCoupon) {
            couponValidator.validateBxGyCoupon((BxGyCoupon) coupon);
        }
        
        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon created successfully with ID: {}", savedCoupon.getId());
        
        return mapToResponse(savedCoupon);
    }
    
    public Page<CouponResponse> getAllCoupons(Boolean isActive, CouponType type, Pageable pageable) {
        log.info("Fetching coupons - isActive: {}, type: {}", isActive, type);
        
        Page<Coupon> coupons;
        if (isActive != null && type != null) {
            coupons = couponRepository.findByIsActiveAndType(isActive, type, pageable);
        } else if (isActive != null) {
            coupons = couponRepository.findByIsActive(isActive, pageable);
        } else if (type != null) {
            coupons = couponRepository.findByType(type, pageable);
        } else {
            coupons = couponRepository.findAll(pageable);
        }
        
        return coupons.map(this::mapToResponse);
    }
    
    public CouponResponse getCouponById(Long id) {
        log.info("Fetching coupon by ID: {}", id);
        
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new CouponNotFoundException(id));
        
        return mapToResponse(coupon);
    }
    
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        log.info("Updating coupon with ID: {}", id);
        
        Coupon existingCoupon = couponRepository.findById(id)
            .orElseThrow(() -> new CouponNotFoundException(id));
        
        // Check for duplicate code if code is being changed
        if (!existingCoupon.getCode().equals(request.getCode()) && 
            couponRepository.existsByCode(request.getCode())) {
            throw new DuplicateCouponCodeException(request.getCode());
        }
        
        // Update fields
        updateCouponFromRequest(existingCoupon, request);
        
        // Validate
        couponValidator.validateCouponFields(existingCoupon);
        
        // Type-specific validation
        if (existingCoupon instanceof CartWiseCoupon) {
            couponValidator.validateCartWiseCoupon((CartWiseCoupon) existingCoupon, 
                ((CartWiseCoupon) existingCoupon).getMinCartAmount());
        } else if (existingCoupon instanceof ProductWiseCoupon) {
            couponValidator.validateProductWiseCoupon((ProductWiseCoupon) existingCoupon);
        } else if (existingCoupon instanceof BxGyCoupon) {
            couponValidator.validateBxGyCoupon((BxGyCoupon) existingCoupon);
        }
        
        Coupon updatedCoupon = couponRepository.save(existingCoupon);
        log.info("Coupon updated successfully with ID: {}", updatedCoupon.getId());
        
        return mapToResponse(updatedCoupon);
    }
    
    @Transactional
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon with ID: {}", id);
        
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new CouponNotFoundException(id));
        
        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully with ID: {}", id);
    }
    
    private Coupon createCouponFromRequest(CouponRequest request) {
        CouponType type = request.getType();
        
        if (type == CouponType.CART_WISE) {
            CartWiseCoupon coupon = new CartWiseCoupon();
            setBaseCouponFields(coupon, request);
            coupon.setMinCartAmount(request.getMinCartAmount());
            coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
            return coupon;
        } else if (type == CouponType.PRODUCT_WISE) {
            ProductWiseCoupon coupon = new ProductWiseCoupon();
            setBaseCouponFields(coupon, request);
            coupon.setApplicableProductIds(request.getApplicableProductIds() != null ? 
                request.getApplicableProductIds() : new HashSet<>());
            coupon.setMinQuantity(request.getMinQuantity());
            coupon.setMaxQuantity(request.getMaxQuantity());
            return coupon;
        } else if (type == CouponType.BXGY) {
            BxGyCoupon coupon = new BxGyCoupon();
            setBaseCouponFields(coupon, request);
            coupon.setBuyProductIds(request.getBuyProductIds() != null ? 
                request.getBuyProductIds() : new HashSet<>());
            coupon.setBuyQuantity(request.getBuyQuantity());
            coupon.setGetProductIds(request.getGetProductIds() != null ? 
                request.getGetProductIds() : new HashSet<>());
            coupon.setGetQuantity(request.getGetQuantity());
            coupon.setRepetitionLimit(request.getRepetitionLimit());
            coupon.setBxGyDiscountType(request.getBxGyDiscountType());
            return coupon;
        }
        
        throw new InvalidCouponException("Invalid coupon type: " + type);
    }
    
    private void setBaseCouponFields(Coupon coupon, CouponRequest request) {
        coupon.setCode(request.getCode());
        coupon.setName(request.getName());
        coupon.setType(request.getType());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
    }
    
    private void updateCouponFromRequest(Coupon coupon, CouponRequest request) {
        coupon.setCode(request.getCode());
        coupon.setName(request.getName());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setIsActive(request.getIsActive());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        
        if (coupon instanceof CartWiseCoupon cartWiseCoupon) {
            cartWiseCoupon.setMinCartAmount(request.getMinCartAmount());
            cartWiseCoupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        } else if (coupon instanceof ProductWiseCoupon productWiseCoupon) {
            if (request.getApplicableProductIds() != null) {
                productWiseCoupon.setApplicableProductIds(request.getApplicableProductIds());
            }
            productWiseCoupon.setMinQuantity(request.getMinQuantity());
            productWiseCoupon.setMaxQuantity(request.getMaxQuantity());
        } else if (coupon instanceof BxGyCoupon bxGyCoupon) {
            if (request.getBuyProductIds() != null) {
                bxGyCoupon.setBuyProductIds(request.getBuyProductIds());
            }
            if (request.getGetProductIds() != null) {
                bxGyCoupon.setGetProductIds(request.getGetProductIds());
            }
            bxGyCoupon.setBuyQuantity(request.getBuyQuantity());
            bxGyCoupon.setGetQuantity(request.getGetQuantity());
            bxGyCoupon.setRepetitionLimit(request.getRepetitionLimit());
            bxGyCoupon.setBxGyDiscountType(request.getBxGyDiscountType());
        }
    }
    
    private CouponResponse mapToResponse(Coupon coupon) {
        CouponResponse.CouponResponseBuilder builder = CouponResponse.builder()
            .id(coupon.getId())
            .code(coupon.getCode())
            .name(coupon.getName())
            .type(coupon.getType())
            .discountType(coupon.getDiscountType())
            .discountValue(coupon.getDiscountValue())
            .isActive(coupon.getIsActive())
            .startDate(coupon.getStartDate())
            .endDate(coupon.getEndDate())
            .maxUsage(coupon.getMaxUsage())
            .currentUsage(coupon.getCurrentUsage())
            .maxUsagePerUser(coupon.getMaxUsagePerUser())
            .createdAt(coupon.getCreatedAt())
            .updatedAt(coupon.getUpdatedAt());
        
        if (coupon instanceof CartWiseCoupon cartWiseCoupon) {
            builder.minCartAmount(cartWiseCoupon.getMinCartAmount())
                   .maxDiscountAmount(cartWiseCoupon.getMaxDiscountAmount());
        } else if (coupon instanceof ProductWiseCoupon productWiseCoupon) {
            builder.applicableProductIds(productWiseCoupon.getApplicableProductIds())
                   .minQuantity(productWiseCoupon.getMinQuantity())
                   .maxQuantity(productWiseCoupon.getMaxQuantity());
        } else if (coupon instanceof BxGyCoupon bxGyCoupon) {
            builder.buyProductIds(bxGyCoupon.getBuyProductIds())
                   .buyQuantity(bxGyCoupon.getBuyQuantity())
                   .getProductIds(bxGyCoupon.getGetProductIds())
                   .getQuantity(bxGyCoupon.getGetQuantity())
                   .repetitionLimit(bxGyCoupon.getRepetitionLimit())
                   .bxGyDiscountType(bxGyCoupon.getBxGyDiscountType());
        }
        
        return builder.build();
    }
}

