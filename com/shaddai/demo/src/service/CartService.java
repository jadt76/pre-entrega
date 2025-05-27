package com.shaddai.demo.src.service;

import com.shaddai.demo.src.model.*;
import com.shaddai.demo.src.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Obtener o crear carrito para un usuario
    public Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<Cart> existingCart = cartRepository.findByUser(user);

        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        }
    }

    // Agregar producto al carrito
    public Cart addProductToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock disponible
        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Verificar si el producto ya está en el carrito
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Si ya existe, actualizar cantidad
            CartItem item = existingItem.get();
            Integer newQuantity = item.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Stock insuficiente para la cantidad solicitada");
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Si no existe, crear nuevo item
            CartItem newItem = new CartItem(cart, product, quantity, product.getPrice());
            cartItemRepository.save(newItem);
        }

        // Actualizar fecha de modificación del carrito
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // Actualizar cantidad de un producto en el carrito
    public Cart updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        if (newQuantity <= 0) {
            return removeProductFromCart(userId, productId);
        }

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        // Verificar stock disponible
        if (product.getStock() < newQuantity) {
            throw new RuntimeException("Stock insuficiente");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // Remover producto del carrito
    public Cart removeProductFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        cartItemRepository.delete(item);

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // Vaciar carrito completamente
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart(cart);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    // Obtener carrito de un usuario
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}