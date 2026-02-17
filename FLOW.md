# E-COMMERCE CORE FLOWS

## 1. USER REGISTRATION/LOGIN FLOW

### Registration

```
STEP 1: User clicks "Register" button
STEP 2: Angular redirects to OAuth2 provider (Auth0/Google)
STEP 3: User completes registration on OAuth2 provider
STEP 4: OAuth2 provider redirects back with authorization code
STEP 5: Angular exchanges code for access/refresh tokens
STEP 6: Angular stores tokens securely
STEP 7: Angular calls User Service to create user profile
STEP 8: User Service saves user data to PostgreSQL
STEP 9: User is redirected to dashboard/home page
```

### Login

```
STEP 1: User clicks "Login" button
STEP 2: Angular redirects to OAuth2 provider
STEP 3: User enters credentials on OAuth2 provider
STEP 4: OAuth2 provider redirects back with authorization code
STEP 5: Angular exchanges code for tokens
STEP 6: Angular stores tokens
STEP 7: Angular fetches user profile from User Service
STEP 8: User Service checks Redis cache for user data
STEP 9: If not in cache, fetch from PostgreSQL and cache in Redis
STEP 10: User is logged in and redirected to home page
```

## 2. BROWSE PRODUCTS FLOW

### View Product List

```
STEP 1: User navigates to products page
STEP 2: Angular calls Product Service via API Gateway
STEP 3: API Gateway routes request to Product Service
STEP 4: Product Service checks Redis cache for product list
STEP 5: If cache miss, query PostgreSQL for products
STEP 6: Cache results in Redis with TTL (10 minutes)
STEP 7: Return product list to Angular
STEP 8: Angular displays products in grid/list view
```

### Search Products

```
STEP 1: User enters search term
STEP 2: Angular debounces input and calls Product Service
STEP 3: Product Service checks Redis cache for search results
STEP 4: If cache miss, query PostgreSQL with LIKE/full-text search
STEP 5: Cache search results in Redis
STEP 6: Return filtered products to Angular
STEP 7: Angular updates product display with search results
```

### View Product Details

```
STEP 1: User clicks on product
STEP 2: Angular navigates to product detail page
STEP 3: Angular calls Product Service for specific product
STEP 4: Product Service checks Redis cache for product details
STEP 5: If cache miss, query PostgreSQL for product
STEP 6: Cache product details in Redis
STEP 7: Return product details to Angular
STEP 8: Angular displays product details, images, reviews
```

## 3. ADD TO CART FLOW

### Anonymous User (Guest Cart)

```
STEP 1: User clicks "Add to Cart" button
STEP 2: Angular stores item in local storage/memory
STEP 3: Angular updates cart icon with item count
STEP 4: Show success message to user
```

### Authenticated User

```
STEP 1: User clicks "Add to Cart" button
STEP 2: Angular calls Order Service (Cart endpoint) via API Gateway
STEP 3: API Gateway validates JWT token
STEP 4: Order Service receives add to cart request
STEP 5: Order Service checks if user has existing cart in PostgreSQL
STEP 6: If no cart, create new cart record
STEP 7: Add/update cart item in PostgreSQL
STEP 8: Update cart cache in Redis
STEP 9: Publish "item added to cart" event to Kafka
STEP 10: Return updated cart to Angular
STEP 11: Angular updates UI with new cart state
```

### View Cart

```
STEP 1: User clicks cart icon
STEP 2: If anonymous, show items from local storage
STEP 3: If authenticated, Angular calls Order Service for cart
STEP 4: Order Service checks Redis cache for user's cart
STEP 5: If cache miss, query PostgreSQL for cart items
STEP 6: Cache cart data in Redis
STEP 7: Return cart with item details to Angular
STEP 8: Angular displays cart with products, quantities, prices
```

## 4. CHECKOUT & ORDER PROCESSING FLOW

### Initiate Checkout

```
STEP 1: User clicks "Checkout" from cart
STEP 2: Angular checks if user is authenticated
STEP 3: If not authenticated, redirect to login
STEP 4: If authenticated, navigate to checkout page
STEP 5: Angular fetches user profile and saved addresses
STEP 6: Angular displays checkout form with cart summary
```

### Process Order

```
STEP 1: User fills shipping address and payment method
STEP 2: User clicks "Place Order"
STEP 3: Angular validates form data
STEP 4: Angular calls Order Service to create order
STEP 5: API Gateway validates JWT token
STEP 6: Order Service receives create order request
STEP 7: Order Service starts database transaction
STEP 8: Validate cart items still exist and in stock
STEP 9: Calculate order total (items + tax + shipping)
STEP 10: Create order record in PostgreSQL
STEP 11: Create order items records in PostgreSQL
STEP 12: Update product inventory in PostgreSQL
STEP 13: Clear user's cart from PostgreSQL
STEP 14: Commit database transaction
STEP 15: Cache order details in Redis
STEP 16: Publish "order created" event to Kafka
STEP 17: Return order confirmation to Angular
STEP 18: Angular navigates to order confirmation page
```

### Order Event Processing (Async via Kafka)

```
STEP 1: Order Service publishes "order created" event
STEP 2: Inventory Service consumes event
STEP 3: Inventory Service reserves/reduces stock
STEP 4: Notification Service consumes event
STEP 5: Notification Service sends confirmation email
STEP 6: Payment Service consumes event (if separate)
STEP 7: Payment Service processes payment
STEP 8: Payment Service publishes "payment processed" event
STEP 9: Order Service updates order status to "confirmed"
```

### View Orders

```
STEP 1: User navigates to "My Orders" page
STEP 2: Angular calls Order Service for user's orders
STEP 3: API Gateway validates JWT token
STEP 4: Order Service checks Redis cache for user's orders
STEP 5: If cache miss, query PostgreSQL for orders
STEP 6: Cache order list in Redis with short TTL
STEP 7: Return orders with status and details to Angular
STEP 8: Angular displays order history with status updates
```

## ERROR HANDLING FLOWS

### Out of Stock

```
STEP 1: User attempts to add out-of-stock item to cart
STEP 2: Order Service checks inventory in PostgreSQL
STEP 3: If insufficient stock, return error response
STEP 4: Angular displays "Out of Stock" message
STEP 5: Angular disables "Add to Cart" button
```

### Payment Failure

```
STEP 1: Payment processing fails during checkout
STEP 2: Payment Service publishes "payment failed" event
STEP 3: Order Service updates order status to "failed"
STEP 4: Order Service restores inventory
STEP 5: Angular displays payment error message
STEP 6: User can retry with different payment method
```

### Service Unavailable

```
STEP 1: Microservice is down or unresponsive
STEP 2: API Gateway circuit breaker activates
STEP 3: Angular receives 503 Service Unavailable
STEP 4: Angular displays user-friendly error message
STEP 5: Angular can retry request after delay
STEP 6: Fall back to cached data if available
```