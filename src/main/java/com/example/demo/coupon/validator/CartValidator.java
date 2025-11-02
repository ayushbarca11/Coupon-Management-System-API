package com.example.demo.coupon.validator;

import com.example.demo.coupon.dto.CartItemRequest;
import com.example.demo.coupon.exception.InvalidCouponException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class CartValidator {
    
    /**
     * Validate cart items
     */
    public void validateCartItems(List<CartItemRequest> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new InvalidCouponException("Cart items cannot be empty");
        }
        
        for (CartItemRequest item : cartItems) {
            if (item.getProductId() == null) {
                throw new InvalidCouponException("Product ID is required for all cart items");
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new InvalidCouponException(
                    String.format("Quantity must be positive for product %d", item.getProductId()));
            }
            
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidCouponException(
                    String.format("Unit price must be positive for product %d", item.getProductId()));
            }
        }
    }
    
    /**
     * Calculate cart total
     */
    public BigDecimal calculateCartTotal(List<CartItemRequest> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return cartItems.stream()
            .map(item -> item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

