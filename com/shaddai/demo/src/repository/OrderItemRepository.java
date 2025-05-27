package com.shaddai.demo.src.repository;

import com.shaddai.demo.src.model.OrderItem;
import com.shaddai.demo.src.model.Order;
import com.shaddai.demo.src.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Buscar items por pedido
    List<OrderItem> findByOrder(Order order);

    // Buscar items por producto (para estadísticas de ventas)
    List<OrderItem> findByProduct(Product product);

    // Productos más vendidos
    @Query("SELECT oi.product, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "GROUP BY oi.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findMostSoldProducts();
}