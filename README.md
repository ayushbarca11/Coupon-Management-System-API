# Coupon Management System

A comprehensive RESTful API system for managing discount coupons in an e-commerce platform. Supports multiple coupon types.
 **Access API**:
   - **API Base URL**: http://localhost:8080/api/coupons
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
 

## API Endpoints

### Coupon Management

- `POST /api/coupons` - Create a new coupon
- `GET /api/coupons` - Get all coupons (with pagination and filters)
- `GET /api/coupons/{id}` - Get coupon by ID
- `PUT /api/coupons/{id}` - Update coupon
- `DELETE /api/coupons/{id}` - Delete coupon

### Coupon Application

- `POST /api/coupons/applicable-coupons` - Get all applicable coupons for a cart
- `POST /api/coupons/apply-coupon/{couponId}` - Apply a specific coupon to cart

## Architecture & Design

### Technology Stack

- **Framework**: Spring Boot 
- **Language**: Java 17
- **Database**: MySQL 
- **Build Tool**: Maven

### Design Patterns

The system employs several design patterns for maintainability and extensibility:

 **Strategy Pattern** - Different coupon types (CART_WISE, PRODUCT_WISE, BXGY) are handled by separate strategy classes:
   - `CartWiseStrategy` - Handles cart-level discounts
   - `ProductWiseStrategy` - Handles product-specific discounts
   - `BxGyStrategy` - Handles Buy X Get Y deals
  


##  Coupon Types & Logic

### 1. Cart-wise Coupons

Apply discounts based on the entire cart total.

#### Percentage Discount
- **Example**: 10% off on carts over Rs. 100
- **Calculation**: `discount = (cartTotal × discountValue) / 100`
- **Cap**: If `maxDiscountAmount` specified: `min(discount, maxDiscountAmount)`
- **Validation**: Cart total must be >= `minCartAmount`

#### Fixed Amount Discount
- **Example**: Rs. 50 off on carts over Rs. 200
- **Calculation**: `discount = min(discountValue, cartTotal)`
- **Validation**: Cart total must be >= `minCartAmount`

### 2. Product-wise Coupons

Apply discounts to specific products in the cart.

#### Percentage Discount on Products
- **Example**: 20% off on Products 1, 2, 3
- **Calculation**: Sum of `(productPrice × quantity × discountValue) / 100` for each applicable product
- **Quantity Limits**: Can specify `minQuantity` and `maxQuantity`

#### Fixed Amount Discount on Products
- **Example**: Rs. 10 off per unit on Products 4, 5 (minimum 2 units)
- **Calculation**: `discount = min(discountValue × eligibleQuantity, productTotal)`
- **Quantity Limits**: Applied only to quantities within specified range

### 3. BxGy (Buy X Get Y) Coupons

Buy X products from one set, get Y products from another set with discount.

#### Free Products (100% Discount)
- **Example**: Buy 2 from [10, 11, 12], Get 1 from [20, 21] free
- **Calculation**:
  1. Count eligible buy products: `buyCount = sum(quantities of products in buyProductIds)`
  2. Count eligible get products: `getCount = sum(quantities of products in getProductIds)`
  3. Applications: `min(buyCount / buyQuantity, getCount / getQuantity, repetitionLimit)`
  4. Free items: `applications × getQuantity`

#### Percentage Discount on Get Products
- **Example**: Buy 3 from [15, 16], Get 50% off on 2 from [25, 26]
- **Calculation**: Same counting logic, but apply percentage discount to get products instead of making them free

#### Fixed Amount Discount on Get Products
- **Example**: Buy 2 from [30, 31], Get Rs. 20 off on 1 from [40, 41]
- **Calculation**: Apply fixed discount to eligible get products

**Special Handling:**
- Products can appear in both buy and get sets
- A product counts only once per application (buy takes priority)
- Quantities are additive across different products in the same set

**Use Cases**: Bundle deals, cross-selling promotions, seasonal offers

## Implemented Use Cases

### Cart-wise Coupons

 **Case 1.1: Percentage Discount on Cart Total**
- 10% off when cart >= Rs. 100
- Supports maximum discount cap

 **Case 1.2: Fixed Amount Discount on Cart**
- Rs. X off when cart exceeds threshold
- Prevents discount exceeding cart total

 **Case 1.4: Cart-wise with Maximum Discount Cap**
- Limits maximum discount amount
- Example: 20% off, max Rs. 500

### Product-wise Coupons

 **Case 2.1: Percentage Discount on Specific Product**
- 20% off on Product A
- Applied to all eligible products in cart

 **Case 2.2: Fixed Amount Discount on Product**
- Rs. X off per unit or total
- Supports per-unit or total discount

 **Case 2.3: Discount on Multiple Products**
- Same discount on multiple products
- Example: 15% off on Products A, B, C

 **Case 2.4: Minimum Quantity Requirement**
- Require minimum quantity to apply
- Example: 20% off on Product A, minimum 3 units

 **Case 2.5: Maximum Quantity for Discount**
- Limit discount to specific quantity
- Example: 20% off on Product A, max 5 units

### BxGy Coupons

 **Case 3.1: Buy X Get Y Free**
- Buy 2 from set A, get 1 from set B free
- Supports different product sets

 **Case 3.2: BxGy with Repetition Limit**
- Limit how many times deal can repeat
- Example: Buy 2 get 1, max 3 repetitions

 **Case 3.3: BxGy with Same Product in Buy and Get**
- Overlapping product sets supported
- Product counts only once per application

 **Case 3.4: BxGy with Multiple Quantities**
- Asymmetric quantities (Buy 3, Get 2)
- Handles different X and Y values

 **Case 3.5: BxGy with Percentage Discount**
- Percentage discount instead of free
- Example: Buy 2, get 50% off on 1

 **Case 3.6: BxGy with Fixed Amount Discount**
- Fixed discount on get products
- Example: Buy 2, get Rs. 20 off on 1

 **Case 3.7: BxGy Multiple Applications**
- Correct handling of partial quantities
- Handles remainders correctly

 **Case 3.8 & 3.9: Multiple Products in Sets**
- Mix and match products in buy/get sets
- Quantities additive across products

### General Use Cases

 **Case 4.1: Coupon Expiration**
- Start and end date validation
- Expired coupons not applicable

 **Case 4.2: Coupon Usage Limits (Global)**
- Maximum total usage tracking
- Increment on each application

 **Case 4.3: Coupon Usage Limits (Per User)**
- Per-user usage tracking
- Stored in `coupon_usages` table

 **Case 4.4: Coupon Activation Status**
- Enable/disable without deletion
- Only active coupons applicable

 **Case 4.7: Negative Cart Total Prevention**
- Final total never negative
- Validation: `finalTotal = max(0, originalTotal - discount)`

 **Case 4.8: Applicable Coupons API**
- Calculate discount without applying
- Show all applicable coupons with savings

##  Unimplemented / Complex Use Cases

### High Complexity 

 **Case 5.1: Coupon Stacking Rules**
- Complex rule engine for which coupons can stack
- Conflict resolution between multiple coupons


 **Case 5.2: First-time User Coupons**
- Coupons only for new users
- *Requirement*: User registration tracking service

 **Case 5.3: Category-based Discounts**
- Discount on entire product categories
- *Requirement*: Product categorization system

 **Case 5.5: Time-based Coupons**
- Valid only during specific hours/days or weekends
- *Requirement*: Advanced time validation logic

 **Case 5.6: Geographic Restrictions**
- Region-specific coupon validity
- *Requirement*: Location tracking service

 **Case 5.7: Referral Coupons**
- Earned through referral system
- *Requirement*: Referral tracking system

 **Case 5.8: Loyalty Points Coupons**
- Discount based on loyalty points
- *Requirement*: Loyalty points management system

 **Case 5.10: Dynamic Pricing with Coupons**
- Adjust discount based on inventory
- *Requirement*: Real-time inventory tracking

 **Case 5.11: Coupon Combinations (AND/OR Logic)**
- Complex conditional logic
- Example: "10% off if (cart > 100 AND product X) OR (user is premium)"
- *Requirement*: Rule engine implementation

 **Case 5.12: Coupon Auto-application**
- Automatically apply best available coupon
- *Complexity*: Defining "best" coupon logic

 **Case 5.13: Coupon Rollback**
- Undo coupon application


##  Sample Data

The system includes 7 pre-loaded sample coupons:

| Code | Type | Description |
|------|------|-------------|
| CART10 | CART_WISE | 10% off on carts over Rs. 100 (max Rs. 500) |
| FLAT50 | CART_WISE | Rs. 50 off on carts over Rs. 200 |
| PROD20 | PRODUCT_WISE | 20% off on Products 1, 2, 3 |
| PROD10 | PRODUCT_WISE | Rs. 10 off on Products 4, 5 (min 2, max 10 units) |
| BUY2GET1 | BXGY | Buy 2 from [10,11,12], Get 1 from [20,21] free (max 3 reps) |
| BUY3GET50 | BXGY | Buy 3 from [15,16], Get 50% off on 2 from [25,26] (max 2 reps) |
| BUY2GET20 | BXGY | Buy 2 from [30,31], Get Rs. 20 off on 1 from [40,41] (max 5 reps) |

##  Testing

### Using Swagger UI

1. Navigate to: http://localhost:8080/swagger-ui.html


##  Assumptions & Limitations

### Assumptions
- Cart sent as request body (stateless architecture)
- Product information provided in cart request
- User ID provided for usage tracking (no authentication)
- Single currency (INR)
- Sequential discount application

### Current Limitations
- No product catalog integration
- No user authentication
- Basic conflict resolution (first coupon wins)
- No distributed locking for concurrent usage
- Single currency support only
- No inventory checks

##  Future Enhancements

- Enhanced error messages
- Caching layer for active coupons
- Better BxGy logic (different discounts per product)
- Advanced stacking rules with rule engine
- Real-time notifications 
- AI-powered coupon recommendations
- Multi-currency support

