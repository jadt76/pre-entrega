package com.shaddai.demo.src.service;

import com.shaddai.demo.src.model.*;
import com.shaddai.demo.src.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    // Crear pedido desde el carrito
    public Order createOrderFromCart(Long userId, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Verificar stock de todos los productos antes de procesar
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getProduct().getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + cartItem.getProduct().getName());
            }
        }

        // Crear el pedido
        Order order = new Order(user, cart.getTotalAmount(), shippingAddress);
        order = orderRepository.save(order);

        // Crear los items del pedido y reducir stock
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
            orderItems.add(orderItem);
            orderItemRepository.save(orderItem);

            // Reducir stock del producto
            productService.reduceStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        order.setItems(orderItems);

        // Limpiar el carrito
        cartItemRepository.deleteByCart(cart);

        return order;
    }

    // Obtener todos los pedidos
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Obtener pedidos de un usuario
    public List<Order> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Obtener pedido por ID
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // Actualizar estado del pedido
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Cancelar pedido
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("El pedido ya está cancelado");
        }

        // Restaurar stock de los productos
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productService.updateStock(product.getId(), product.getStock());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Obtener pedidos por estado
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    // Obtener pedidos recientes (últimos 30 días)
    public List<Order> getRecentOrders() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return orderRepository.findRecentOrders(thirtyDaysAgo);
    }

    // Obtener pedidos por rango de fechas
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }
}