package com.example.demo.coupon.service.strategy;

import com.example.demo.coupon.dto.ApplicableCouponResponse;
import com.example.demo.coupon.dto.CartItemRequest;
import com.example.demo.coupon.dto.CartItemResponse;
import com.example.demo.coupon.dto.DiscountBreakdown;
import com.example.demo.coupon.model.BxGyCoupon;
import com.example.demo.coupon.model.Coupon;
import com.example.demo.coupon.model.enums.BxGyDiscountType;
import com.example.demo.coupon.util.DiscountCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BxGyStrategy implements CouponStrategy {
    
    @Override
    public boolean isApplicable(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!(coupon instanceof BxGyCoupon bxGyCoupon)) {
            return false;
        }
        
        // Count products considering overlap (buy takes priority)
        int buyCount = countProductsInSet(cartItems, bxGyCoupon.getBuyProductIds());
        if (buyCount < bxGyCoupon.getBuyQuantity()) {
            return false;
        }
        
        // Count get products excluding those also in buy set
        int getCount = countGetProductsExcludingBuy(cartItems, bxGyCoupon);
        if (getCount < bxGyCoupon.getGetQuantity()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public BigDecimal calculateDiscount(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!isApplicable(coupon, cartItems, cartTotal)) {
            return BigDecimal.ZERO;
        }
        
        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
        
        // Calculate how many times the deal can be applied
        int applications = calculateApplications(bxGyCoupon, cartItems);
        
        if (applications <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Get products from get set (excluding those also in buy set) and calculate discount
        // Build map of available get products, excluding quantities used for buy
        Map<Long, Integer> getProductAvailableQuantities = new HashMap<>();
        Set<Long> overlapProducts = new HashSet<>(bxGyCoupon.getBuyProductIds());
        overlapProducts.retainAll(bxGyCoupon.getGetProductIds());
        
        // Calculate how many buy products we need
        int totalBuyQuantityNeeded = applications * bxGyCoupon.getBuyQuantity();
        Map<Long, Integer> buyProductsUsed = new HashMap<>();
        int remainingBuyNeeded = totalBuyQuantityNeeded;
        
        // First, allocate products for buy (including overlap products)
        for (CartItemRequest item : cartItems) {
            if (remainingBuyNeeded <= 0) break;
            if (bxGyCoupon.getBuyProductIds().contains(item.getProductId())) {
                int quantityForBuy = Math.min(item.getQuantity(), remainingBuyNeeded);
                buyProductsUsed.put(item.getProductId(), quantityForBuy);
                remainingBuyNeeded -= quantityForBuy;
            }
        }
        
        // Now calculate available get quantities (excluding those used for buy)
        for (CartItemRequest item : cartItems) {
            if (bxGyCoupon.getGetProductIds().contains(item.getProductId())) {
                int availableQuantity = item.getQuantity();
                // If product is in both sets, subtract quantity used for buy
                if (overlapProducts.contains(item.getProductId())) {
                    availableQuantity -= buyProductsUsed.getOrDefault(item.getProductId(), 0);
                }
                if (availableQuantity > 0) {
                    getProductAvailableQuantities.put(item.getProductId(), availableQuantity);
                }
            }
        }
        
        // Calculate discount on available get products
        BigDecimal totalDiscount = BigDecimal.ZERO;
        int totalGetQuantity = applications * bxGyCoupon.getGetQuantity();
        
        // Build get product map for price lookup
        Map<Long, CartItemRequest> getProductMap = cartItems.stream()
            .filter(item -> bxGyCoupon.getGetProductIds().contains(item.getProductId()))
            .collect(Collectors.toMap(
                CartItemRequest::getProductId,
                item -> item,
                (existing, replacement) -> existing));
        
        for (Map.Entry<Long, Integer> entry : getProductAvailableQuantities.entrySet()) {
            if (totalGetQuantity <= 0) {
                break;
            }
            
            Long productId = entry.getKey();
            int availableQuantity = entry.getValue();
            CartItemRequest item = getProductMap.get(productId);
            
            int quantityToDiscount = Math.min(availableQuantity, totalGetQuantity);
            BigDecimal itemTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(quantityToDiscount));
            
            BigDecimal itemDiscount = BigDecimal.ZERO;
            if (bxGyCoupon.getBxGyDiscountType() == BxGyDiscountType.FREE) {
                itemDiscount = itemTotal;
            } else if (bxGyCoupon.getBxGyDiscountType() == BxGyDiscountType.PERCENTAGE) {
                itemDiscount = DiscountCalculator.calculatePercentageDiscount(
                    itemTotal, coupon.getDiscountValue());
            } else if (bxGyCoupon.getBxGyDiscountType() == BxGyDiscountType.FIXED_AMOUNT) {
                // Fixed discount per unit: multiply discountValue by quantity
                BigDecimal totalFixedDiscount = coupon.getDiscountValue()
                    .multiply(BigDecimal.valueOf(quantityToDiscount));
                itemDiscount = DiscountCalculator.calculateFixedDiscount(itemTotal, totalFixedDiscount);
            }
            
            totalDiscount = totalDiscount.add(itemDiscount);
            totalGetQuantity -= quantityToDiscount;
        }
        
        return DiscountCalculator.round(totalDiscount);
    }
    
    @Override
    public ApplicableCouponResponse getApplicabilityInfo(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        boolean applicable = isApplicable(coupon, cartItems, cartTotal);
        
        if (!applicable) {
            BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
            int buyCount = countProductsInSet(cartItems, bxGyCoupon.getBuyProductIds());
            int getCount = countGetProductsExcludingBuy(cartItems, bxGyCoupon);
            
            String message = String.format(
                "Not applicable: Need %d buy products (have %d) and %d get products (have %d)",
                bxGyCoupon.getBuyQuantity(), buyCount,
                bxGyCoupon.getGetQuantity(), getCount);
            
            return ApplicableCouponResponse.builder()
                .couponId(coupon.getId())
                .couponCode(coupon.getCode())
                .couponName(coupon.getName())
                .couponType(coupon.getType())
                .totalDiscount(BigDecimal.ZERO)
                .isApplicable(false)
                .applicabilityMessage(message)
                .build();
        }
        
        BigDecimal discount = calculateDiscount(coupon, cartItems, cartTotal);
        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
        int applications = calculateApplications(bxGyCoupon, cartItems);
        
        DiscountBreakdown breakdown = DiscountBreakdown.builder()
            .cartTotal(cartTotal)
            .discountAmount(discount)
            .buyQuantity(bxGyCoupon.getBuyQuantity())
            .getQuantity(bxGyCoupon.getGetQuantity())
            .applications(applications)
            .build();
        
        return ApplicableCouponResponse.builder()
            .couponId(coupon.getId())
            .couponCode(coupon.getCode())
            .couponName(coupon.getName())
            .couponType(coupon.getType())
            .totalDiscount(discount)
            .discountBreakdown(breakdown)
            .isApplicable(true)
            .applicabilityMessage(String.format("Coupon applicable: %d application(s)", applications))
            .build();
    }
    
    @Override
    public List<CartItemResponse> applyCoupon(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        if (!isApplicable(coupon, cartItems, cartTotal)) {
            return cartItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        }
        
        BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
        int applications = calculateApplications(bxGyCoupon, cartItems);
        
        // Build result list with proper handling of product overlap
        List<CartItemResponse> result = new ArrayList<>();
        
        // Calculate products used for buy and available for get
        Set<Long> overlapProducts = new HashSet<>(bxGyCoupon.getBuyProductIds());
        overlapProducts.retainAll(bxGyCoupon.getGetProductIds());
        
        int totalBuyQuantityNeeded = applications * bxGyCoupon.getBuyQuantity();
        Map<Long, Integer> buyProductsUsed = new HashMap<>();
        int remainingBuyNeeded = totalBuyQuantityNeeded;
        
        // First pass: allocate products for buy (including overlap products)
        for (CartItemRequest item : cartItems) {
            if (remainingBuyNeeded <= 0) break;
            if (bxGyCoupon.getBuyProductIds().contains(item.getProductId())) {
                int quantityForBuy = Math.min(item.getQuantity(), remainingBuyNeeded);
                buyProductsUsed.put(item.getProductId(), 
                    buyProductsUsed.getOrDefault(item.getProductId(), 0) + quantityForBuy);
                remainingBuyNeeded -= quantityForBuy;
            }
        }
        
        // Calculate available get quantities (excluding those used for buy)
        Map<Long, Integer> getProductsAvailable = new HashMap<>();
        for (CartItemRequest item : cartItems) {
            if (bxGyCoupon.getGetProductIds().contains(item.getProductId())) {
                int availableQuantity = item.getQuantity();
                // If product is in both sets, subtract quantity used for buy
                if (overlapProducts.contains(item.getProductId())) {
                    availableQuantity -= buyProductsUsed.getOrDefault(item.getProductId(), 0);
                }
                if (availableQuantity > 0) {
                    getProductsAvailable.put(item.getProductId(), availableQuantity);
                }
            }
        }
        
        // Process all items
        int remainingGetQuantity = applications * bxGyCoupon.getGetQuantity();
        
        for (CartItemRequest item : cartItems) {
            boolean isGetProduct = bxGyCoupon.getGetProductIds().contains(item.getProductId());
            boolean isBuyProduct = bxGyCoupon.getBuyProductIds().contains(item.getProductId());
            
            BigDecimal originalPrice = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
            
            BigDecimal discount = BigDecimal.ZERO;
            
            if (isGetProduct && remainingGetQuantity > 0) {
                // Determine available quantity for discount
                int availableForGet = item.getQuantity();
                if (overlapProducts.contains(item.getProductId())) {
                    // Product is in both sets - subtract quantity used for buy
                    availableForGet -= buyProductsUsed.getOrDefault(item.getProductId(), 0);
                }
                
                int quantityToDiscount = Math.min(Math.max(0, availableForGet), remainingGetQuantity);
                if (quantityToDiscount > 0) {
                    BigDecimal itemTotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(quantityToDiscount));
                    
                    if (bxGyCoupon.getBxGyDiscountType() == BxGyDiscountType.FREE) {
                        discount = itemTotal;
                    } else if (bxGyCoupon.getBxGyDiscountType() == BxGyDiscountType.PERCENTAGE) {
                        discount = DiscountCalculator.calculatePercentageDiscount(
                            itemTotal, coupon.getDiscountValue());
                    } else if (bxGyCoupon.getBxGyDiscountType() == BxGyDiscountType.FIXED_AMOUNT) {
                        // Fixed discount per unit
                        BigDecimal totalFixedDiscount = coupon.getDiscountValue()
                            .multiply(BigDecimal.valueOf(quantityToDiscount));
                        discount = DiscountCalculator.calculateFixedDiscount(itemTotal, totalFixedDiscount);
                    }
                    
                    remainingGetQuantity -= quantityToDiscount;
                }
            }
            
            BigDecimal discountedPrice = originalPrice.subtract(discount);
            
            result.add(CartItemResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .originalPrice(DiscountCalculator.round(originalPrice))
                .discountedPrice(DiscountCalculator.round(discountedPrice))
                .discountApplied(DiscountCalculator.round(discount))
                .build());
        }
        
        return result;
    }
    
    @Override
    public BigDecimal getTotalDiscountApplied(Coupon coupon, List<CartItemRequest> cartItems, BigDecimal cartTotal) {
        return calculateDiscount(coupon, cartItems, cartTotal);
    }
    
    /**
     * Count total quantity of products in the given set
     */
    private int countProductsInSet(List<CartItemRequest> cartItems, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        
        return cartItems.stream()
            .filter(item -> productIds.contains(item.getProductId()))
            .mapToInt(CartItemRequest::getQuantity)
            .sum();
    }
    
    /**
     * Calculate how many times the BxGy deal can be applied
     * Handles product overlap: products in both sets count only for buy
     */
    private int calculateApplications(BxGyCoupon bxGyCoupon, List<CartItemRequest> cartItems) {
        int buyCount = countProductsInSet(cartItems, bxGyCoupon.getBuyProductIds());
        // Get products excluding those also in buy set
        int getCount = countGetProductsExcludingBuy(cartItems, bxGyCoupon);
        
        int buyApplications = buyCount / bxGyCoupon.getBuyQuantity();
        int getApplications = getCount / bxGyCoupon.getGetQuantity();
        
        // Take minimum, but also consider repetition limit
        int maxApplications = Math.min(buyApplications, getApplications);
        
        // Apply repetition limit
        return Math.min(maxApplications, bxGyCoupon.getRepetitionLimit());
    }
    
    /**
     * Count get products excluding those also in buy set (handles overlap)
     * Products in both sets count only for buy, not get
     */
    private int countGetProductsExcludingBuy(List<CartItemRequest> cartItems, BxGyCoupon bxGyCoupon) {
        Set<Long> overlapProducts = new HashSet<>(bxGyCoupon.getBuyProductIds());
        overlapProducts.retainAll(bxGyCoupon.getGetProductIds());
        
        int totalGetCount = 0;
        
        // Calculate how many buy products we need (worst case: all overlap products used for buy)
        int totalBuyCount = countProductsInSet(cartItems, bxGyCoupon.getBuyProductIds());
        int maxBuyApplications = totalBuyCount / bxGyCoupon.getBuyQuantity();
        int maxBuyQuantityNeeded = maxBuyApplications * bxGyCoupon.getBuyQuantity();
        
        // Allocate buy quantities to overlap products first
        Map<Long, Integer> overlapUsedForBuy = new HashMap<>();
        int remainingBuyNeeded = maxBuyQuantityNeeded;
        
        for (CartItemRequest item : cartItems) {
            if (remainingBuyNeeded <= 0) break;
            if (overlapProducts.contains(item.getProductId())) {
                int usedForBuy = Math.min(item.getQuantity(), remainingBuyNeeded);
                overlapUsedForBuy.put(item.getProductId(), usedForBuy);
                remainingBuyNeeded -= usedForBuy;
            }
        }
        
        // Count get products, excluding overlap products used for buy
        for (CartItemRequest item : cartItems) {
            if (bxGyCoupon.getGetProductIds().contains(item.getProductId())) {
                if (overlapProducts.contains(item.getProductId())) {
                    // Product is in both sets - subtract quantity used for buy
                    int availableForGet = item.getQuantity() - overlapUsedForBuy.getOrDefault(item.getProductId(), 0);
                    totalGetCount += Math.max(0, availableForGet);
                } else {
                    // Product is only in get set
                    totalGetCount += item.getQuantity();
                }
            }
        }
        
        return totalGetCount;
    }
    
    private CartItemResponse mapToResponse(CartItemRequest item) {
        BigDecimal originalPrice = item.getUnitPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity()));
        
        return CartItemResponse.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .originalPrice(DiscountCalculator.round(originalPrice))
            .discountedPrice(DiscountCalculator.round(originalPrice))
            .discountApplied(BigDecimal.ZERO)
            .build();
    }
}

