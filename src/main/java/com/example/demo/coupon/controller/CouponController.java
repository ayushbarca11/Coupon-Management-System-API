package com.example.demo.coupon.controller;

import com.example.demo.coupon.dto.*;
import com.example.demo.coupon.model.enums.CouponType;
import com.example.demo.coupon.service.CouponApplicationService;
import com.example.demo.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Management", description = "API for managing and applying discount coupons")
public class CouponController {
    
    private final CouponService couponService;
    private final CouponApplicationService couponApplicationService;
    
    @Operation(summary = "Create a new coupon")
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Get all coupons with pagination and filtering")
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter by coupon type") @RequestParam(required = false) CouponType type,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CouponResponse> coupons = couponService.getAllCoupons(isActive, type, pageable);
        return ResponseEntity.ok(coupons);
    }
    
    @Operation(summary = "Get coupon by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(
            @Parameter(description = "Coupon ID") @PathVariable Long id) {
        CouponResponse response = couponService.getCouponById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update coupon by ID")
    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete coupon by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get all applicable coupons for a cart")
    @PostMapping("/applicable-coupons")
    public ResponseEntity<ApplicableCouponsResponse> getApplicableCoupons(
            @Valid @RequestBody CartRequest cartRequest) {
        ApplicableCouponsResponse response = couponApplicationService.getApplicableCoupons(cartRequest);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Apply a coupon to a cart")
    @PostMapping("/apply-coupon/{couponId}")
    public ResponseEntity<CartResponse> applyCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long couponId,
            @Valid @RequestBody CartRequest cartRequest) {
        CartResponse response = couponApplicationService.applyCoupon(couponId, cartRequest);
        return ResponseEntity.ok(response);
    }
}

