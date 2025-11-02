-- Coupon Management System Database Schema
-- Based on COUPON_SYSTEM_PLAN.md Section 3: Database Schema Design
-- 
-- Note: This schema is automatically created by Hibernate/JPA when using ddl-auto=update
-- This file is provided for reference and manual setup if needed

-- Main Coupons Table (Single Table Inheritance Strategy)
-- All coupon types (CART_WISE, PRODUCT_WISE, BXGY) are stored in this table
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    coupon_type VARCHAR(50) NOT NULL,  -- Discriminator: CART_WISE, PRODUCT_WISE, BXGY
    type VARCHAR(50) NOT NULL,          -- CouponType enum
    discount_type VARCHAR(50) NOT NULL, -- DiscountType enum: PERCENTAGE, FIXED_AMOUNT
    discount_value DECIMAL(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    max_usage INT,                      -- NULL means unlimited
    current_usage INT NOT NULL DEFAULT 0,
    max_usage_per_user INT,             -- NULL means unlimited per user
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- CartWiseCoupon specific fields
    min_cart_amount DECIMAL(10, 2),
    max_discount_amount DECIMAL(10, 2),
    
    -- ProductWiseCoupon specific fields
    min_quantity INT,
    max_quantity INT,
    
    -- BxGyCoupon specific fields
    buy_quantity INT,
    get_quantity INT,
    repetition_limit INT,
    bxgy_discount_type VARCHAR(50),     -- BxGyDiscountType enum: FREE, PERCENTAGE, FIXED_AMOUNT
    
    INDEX idx_code (code),
    INDEX idx_type (type),
    INDEX idx_is_active (is_active),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date),
    INDEX idx_coupon_type (coupon_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product-wise Coupon: Applicable Products Mapping
CREATE TABLE IF NOT EXISTS coupon_applicable_products (
    coupon_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, product_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- BxGy Coupon: Buy Products Mapping
CREATE TABLE IF NOT EXISTS bxgy_buy_products (
    coupon_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, product_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    INDEX idx_buy_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- BxGy Coupon: Get Products Mapping
CREATE TABLE IF NOT EXISTS bxgy_get_products (
    coupon_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, product_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    INDEX idx_get_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coupon Usage Tracking
CREATE TABLE IF NOT EXISTS coupon_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT,
    cart_id VARCHAR(255),
    discount_amount DECIMAL(10, 2),
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_usage (coupon_id, user_id, cart_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_cart_id (cart_id),
    INDEX idx_used_at (used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

