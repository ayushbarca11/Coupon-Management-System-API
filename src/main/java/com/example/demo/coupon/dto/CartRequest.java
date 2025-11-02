package com.example.demo.coupon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequest {
    
    @NotEmpty(message = "Cart items cannot be empty")
    @Valid
    private List<CartItemRequest> cartItems;
    
    private Long userId; // Optional, for usage tracking
}

