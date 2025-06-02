package com.shaddai.demo.src.controller;

import com.shaddai.demo.src.entities.Order;
import com.shaddai.demo.src.entities.OrderStatus;
import com.shaddai.demo.src.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // GET /api/orders - Obtener todas las órdenes con paginación
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderService.findAll(pageable);
        return ResponseEntity.ok(orders);
    }

    // GET /api/orders/{id} - Obtener orden por ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/orders/user/{userId} - Obtener órdenes por usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // GET /api/orders/status/{status} - Obtener órdenes por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.findByStatus(orderStatus);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // POST /api/orders - Crear nueva orden
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        try {
            Order savedOrder = orderService.save(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/orders/{id} - Actualizar orden completa
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id,
                                             @Valid @RequestBody Order orderDetails) {
        Optional<Order> optionalOrder = orderService.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setUser(orderDetails.getUser());
            order.setOrderItems(orderDetails.getOrderItems());
            order.setTotalAmount(orderDetails.getTotalAmount());
            order.setStatus(orderDetails.getStatus());
            order.setShippingAddress(orderDetails.getShippingAddress());

            Order updatedOrder = orderService.save(order);
            return ResponseEntity.ok(updatedOrder);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /api/orders/{id}/status - Actualizar solo el status de la orden
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id,
                                                   @RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            Optional<Order> optionalOrder = orderService.findById(id);

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setStatus(orderStatus);
                Order updatedOrder = orderService.save(order);
                return ResponseEntity.ok(updatedOrder);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /api/orders/{id} - Cancelar/Eliminar orden
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);

        if (order.isPresent()) {
            // En lugar de eliminar, cambiar status a CANCELLED
            Order existingOrder = order.get();
            existingOrder.setStatus(OrderStatus.CANCELLED);
            orderService.save(existingOrder);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/orders/{id}/cancel - Cancelar orden específicamente
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Optional<Order> optionalOrder = orderService.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            // Solo se puede cancelar si está PENDING o CONFIRMED
            if (order.getStatus() == OrderStatus.PENDING ||
                    order.getStatus() == OrderStatus.CONFIRMED) {
                order.setStatus(OrderStatus.CANCELLED);
                Order updatedOrder = orderService.save(order);
                return ResponseEntity.ok(updatedOrder);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/orders/{id}/ship - Marcar orden como enviada
    @PostMapping("/{id}/ship")
    public ResponseEntity<Order> shipOrder(@PathVariable Long id) {
        Optional<Order> optionalOrder = orderService.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus() == OrderStatus.CONFIRMED) {
                order.setStatus(OrderStatus.SHIPPED);
                Order updatedOrder = orderService.save(order);
                return ResponseEntity.ok(updatedOrder);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/orders/{id}/deliver - Marcar orden como entregada
    @PostMapping("/{id}/deliver")
    public ResponseEntity<Order> deliverOrder(@PathVariable Long id) {
        Optional<Order> optionalOrder = orderService.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus() == OrderStatus.SHIPPED) {
                order.setStatus(OrderStatus.DELIVERED);
                Order updatedOrder = orderService.save(order);
                return ResponseEntity.ok(updatedOrder);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}