-- Sample Data for Coupon Management System
-- This file will be automatically loaded when the application starts
-- Make sure spring.sql.init.mode=always is set in application.properties
-- Uses INSERT IGNORE to prevent duplicate key errors if data already exists

-- Clear existing sample data if you want to start fresh (uncomment below)
-- DELETE FROM coupon_usages;
-- DELETE FROM coupon_applicable_products;
-- DELETE FROM bxgy_buy_products;
-- DELETE FROM bxgy_get_products;
-- DELETE FROM coupons WHERE code IN ('CART10', 'FLAT50', 'PROD20', 'PROD10', 'BUY2GET1', 'BUY3GET50', 'BUY2GET20');

-- 1. CART_WISE Coupon - 10% off on carts over Rs. 100
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, min_cart_amount, max_discount_amount, created_at, updated_at) 
VALUES ('CART10', '10% off on carts over Rs. 100', 'CART_WISE', 'CART_WISE', 'PERCENTAGE', 10.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1000, 0, 1, 100.00, 500.00, NOW(), NOW());

SET @cart_coupon_id = IF(LAST_INSERT_ID() = 0, (SELECT id FROM coupons WHERE code = 'CART10'), LAST_INSERT_ID());

-- 2. CART_WISE Coupon - Rs. 50 fixed discount on carts over Rs. 200
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, min_cart_amount, max_discount_amount, created_at, updated_at) 
VALUES ('FLAT50', 'Rs. 50 off on carts over Rs. 200', 'CART_WISE', 'CART_WISE', 'FIXED_AMOUNT', 50.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 500, 0, 2, 200.00, NULL, NOW(), NOW());

-- 3. PRODUCT_WISE Coupon - 20% off on Product IDs 1, 2, 3
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, min_quantity, max_quantity, created_at, updated_at) 
VALUES ('PROD20', '20% off on Products 1, 2, 3', 'PRODUCT_WISE', 'PRODUCT_WISE', 'PERCENTAGE', 20.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 2000, 0, 5, 1, NULL, NOW(), NOW());

SET @prod_coupon_id = IF(LAST_INSERT_ID() = 0, (SELECT id FROM coupons WHERE code = 'PROD20'), LAST_INSERT_ID());

-- Insert applicable products for PRODUCT_WISE coupon (ignore duplicates)
INSERT IGNORE INTO coupon_applicable_products (coupon_id, product_id) VALUES
(@prod_coupon_id, 1),
(@prod_coupon_id, 2),
(@prod_coupon_id, 3);

-- 4. PRODUCT_WISE Coupon - Rs. 10 off per unit on Product IDs 4, 5 (minimum 2 units)
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, min_quantity, max_quantity, created_at, updated_at) 
VALUES ('PROD10', 'Rs. 10 off on Products 4, 5 (min 2 units)', 'PRODUCT_WISE', 'PRODUCT_WISE', 'FIXED_AMOUNT', 10.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1500, 0, 3, 2, 10, NOW(), NOW());

SET @prod_coupon_id2 = IF(LAST_INSERT_ID() = 0, (SELECT id FROM coupons WHERE code = 'PROD10'), LAST_INSERT_ID());

-- Insert applicable products for second PRODUCT_WISE coupon (ignore duplicates)
INSERT IGNORE INTO coupon_applicable_products (coupon_id, product_id) VALUES
(@prod_coupon_id2, 4),
(@prod_coupon_id2, 5);

-- 5. BXGY Coupon - Buy 2 from [10, 11, 12], Get 1 from [20, 21] free (max 3 repetitions)
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, buy_quantity, get_quantity, repetition_limit, bxgy_discount_type, created_at, updated_at) 
VALUES ('BUY2GET1', 'Buy 2 Get 1 Free - Products 10-12 and 20-21', 'BXGY', 'BXGY', 'PERCENTAGE', 100.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 800, 0, 2, 2, 1, 3, 'FREE', NOW(), NOW());

SET @bxgy_coupon_id = IF(LAST_INSERT_ID() = 0, (SELECT id FROM coupons WHERE code = 'BUY2GET1'), LAST_INSERT_ID());

-- Insert buy products for BXGY coupon (ignore duplicates)
INSERT IGNORE INTO bxgy_buy_products (coupon_id, product_id) VALUES
(@bxgy_coupon_id, 10),
(@bxgy_coupon_id, 11),
(@bxgy_coupon_id, 12);

-- Insert get products for BXGY coupon (ignore duplicates)
INSERT IGNORE INTO bxgy_get_products (coupon_id, product_id) VALUES
(@bxgy_coupon_id, 20),
(@bxgy_coupon_id, 21);

-- 6. BXGY Coupon - Buy 3 from [15, 16], Get 50% off on 2 from [25, 26] (max 2 repetitions)
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, buy_quantity, get_quantity, repetition_limit, bxgy_discount_type, created_at, updated_at) 
VALUES ('BUY3GET50', 'Buy 3 Get 50% off on 2 - Products 15-16 and 25-26', 'BXGY', 'BXGY', 'PERCENTAGE', 50.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 600, 0, 1, 3, 2, 2, 'PERCENTAGE', NOW(), NOW());

SET @bxgy_coupon_id2 = IF(LAST_INSERT_ID() = 0, (SELECT id FROM coupons WHERE code = 'BUY3GET50'), LAST_INSERT_ID());

-- Insert buy products for second BXGY coupon (ignore duplicates)
INSERT IGNORE INTO bxgy_buy_products (coupon_id, product_id) VALUES
(@bxgy_coupon_id2, 15),
(@bxgy_coupon_id2, 16);

-- Insert get products for second BXGY coupon (ignore duplicates)
INSERT IGNORE INTO bxgy_get_products (coupon_id, product_id) VALUES
(@bxgy_coupon_id2, 25),
(@bxgy_coupon_id2, 26);

-- 7. BXGY Coupon - Buy 2 from [30, 31], Get Rs. 20 off on 1 from [40, 41] (max 5 repetitions)
INSERT IGNORE INTO coupons (code, name, coupon_type, type, discount_type, discount_value, is_active, start_date, end_date, max_usage, current_usage, max_usage_per_user, buy_quantity, get_quantity, repetition_limit, bxgy_discount_type, created_at, updated_at) 
VALUES ('BUY2GET20', 'Buy 2 Get Rs. 20 off on 1 - Products 30-31 and 40-41', 'BXGY', 'BXGY', 'FIXED_AMOUNT', 20.00, true, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1000, 0, 4, 2, 1, 5, 'FIXED_AMOUNT', NOW(), NOW());

SET @bxgy_coupon_id3 = IF(LAST_INSERT_ID() = 0, (SELECT id FROM coupons WHERE code = 'BUY2GET20'), LAST_INSERT_ID());

-- Insert buy products for third BXGY coupon (ignore duplicates)
INSERT IGNORE INTO bxgy_buy_products (coupon_id, product_id) VALUES
(@bxgy_coupon_id3, 30),
(@bxgy_coupon_id3, 31);

-- Insert get products for third BXGY coupon (ignore duplicates)
INSERT IGNORE INTO bxgy_get_products (coupon_id, product_id) VALUES
(@bxgy_coupon_id3, 40),
(@bxgy_coupon_id3, 41);

-- Summary of Sample Coupons:
-- 1. CART10 - 10% off, min cart Rs. 100, max discount Rs. 500
-- 2. FLAT50 - Rs. 50 off, min cart Rs. 200
-- 3. PROD20 - 20% off on products 1, 2, 3
-- 4. PROD10 - Rs. 10 off on products 4, 5 (min 2, max 10 units)
-- 5. BUY2GET1 - Buy 2 from [10,11,12], get 1 from [20,21] free (max 3 reps)
-- 6. BUY3GET50 - Buy 3 from [15,16], get 50% off on 2 from [25,26] (max 2 reps)
-- 7. BUY2GET20 - Buy 2 from [30,31], get Rs. 20 off on 1 from [40,41] (max 5 reps)

