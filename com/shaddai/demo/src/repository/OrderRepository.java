package com.shaddai.demo.src.repository;

import com.shaddai.demo.src.model.Order;
import com.shaddai.demo.src.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Buscar pedidos por usuario
    List<Order> findByUser(User user);

    // Buscar pedidos por usuario ordenados por fecha
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // Buscar pedidos por estado
    List<Order> findByStatus(Order.OrderStatus status);

    // Buscar pedidos por rango de fechas
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Buscar pedidos por usuario y estado
    List<Order> findByUserAndStatus(User user, Order.OrderStatus status);

    // Pedidos recientes (últimos 30 días)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :thirtyDaysAgo ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(LocalDateTime thirtyDaysAgo);
}