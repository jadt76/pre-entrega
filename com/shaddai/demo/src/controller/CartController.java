package com.shaddai.demo.src.controller;

import com.shaddai.demo.src.entities.Cart;
import com.shaddai.demo.src.entities.CartItem;
import com.shaddai.demo.src.entities.Product;
import com.shaddai.demo.src.service.CartService;
import com.shaddai.demo.src.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    // GET /api/cart/user/{userId} - Obtener carrito por usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCartByUser(@PathVariable Long userId) {
        Optional<Cart> cart = cartService.findByUserId(userId);
        return cart.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/cart/user/{userId}/items - Agregar producto al carrito
    @PostMapping("/user/{userId}/items")
    public ResponseEntity<Cart> addItemToCart(@PathVariable Long userId,
                                              @RequestBody AddItemRequest request) {
        Optional<Product> product = productService.findById(request.getProductId());

        if (!product.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Verificar stock disponible
        if (product.get().getStock() < request.getQuantity()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Stock insuficiente
        }

        Cart updatedCart = cartService.addItemToCart(userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    // PUT /api/cart/user/{userId}/items/{productId} - Actualizar cantidad de un item
    @PutMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Cart> updateItemQuantity(@PathVariable Long userId,
                                                   @PathVariable Long productId,
                                                   @RequestParam Integer quantity) {
        if (quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Product> product = productService.findById(productId);
        if (!product.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Verificar stock disponible
        if (product.get().getStock() < quantity) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Stock insuficiente
        }

        try {
            Cart updatedCart = cartService.updateItemQuantity(userId, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/cart/user/{userId}/items/{productId} - Remover item del carrito
    @DeleteMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable Long userId,
                                                   @PathVariable Long productId) {
        try {
            Cart updatedCart = cartService.removeItemFromCart(userId, productId);
            return ResponseEntity.ok(updatedCart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/cart/user/{userId} - Limpiar carrito completo
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/cart/user/{userId}/total - Obtener total del carrito
    @GetMapping("/user/{userId}/total")
    public ResponseEntity<CartTotalResponse> getCartTotal(@PathVariable Long userId) {
        Optional<Cart> cart = cartService.findByUserId(userId);

        if (cart.isPresent()) {
            double total = cart.get().getCartItems().stream()
                    .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                    .sum();

            CartTotalResponse response = new CartTotalResponse(total, cart.get().getCartItems().size());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(new CartTotalResponse(0.0, 0));
        }
    }

    // POST /api/cart/user/{userId}/checkout - Convertir carrito en orden
    @PostMapping("/user/{userId}/checkout")
    public ResponseEntity<String> checkout(@PathVariable Long userId,
                                           @RequestBody CheckoutRequest request) {
        try {
            // Aquí se implementaría la lógica de checkout
            // Por ahora solo limpiamos el carrito
            cartService.clearCart(userId);
            return ResponseEntity.ok("Checkout successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Checkout failed: " + e.getMessage());
        }
    }

    // Clases internas para requests y responses
    public static class AddItemRequest {
        private Long productId;
        private Integer quantity;

        // Constructors
        public AddItemRequest() {}

        public AddItemRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class CheckoutRequest {
        private String shippingAddress;
        private String paymentMethod;

        // Constructors
        public CheckoutRequest() {}

        public CheckoutRequest(String shippingAddress, String paymentMethod) {
            this.shippingAddress = shippingAddress;
            this.paymentMethod = paymentMethod;
        }

        // Getters and Setters
        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

    public static class CartTotalResponse {
        private Double total;
        private Integer itemCount;

        // Constructors
        public CartTotalResponse() {}

        public CartTotalResponse(Double total, Integer itemCount) {
            this.total = total;
            this.itemCount = itemCount;
        }

        // Getters and Setters
        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }

        public Integer getItemCount() {
            return itemCount;
        }

        public void setItemCount(Integer itemCount) {
            this.itemCount = itemCount;
        }
    }
}