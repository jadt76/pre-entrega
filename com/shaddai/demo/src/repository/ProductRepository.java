package com.shaddai.demo.src.repository;

import com.shaddai.demo.src.model.Product;
import com.shaddai.demo.src.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar productos activos
    List<Product> findByActiveTrue();

    // Buscar por categoría
    List<Product> findByCategory(Category category);

    // Buscar por categoría y que estén activos
    List<Product> findByCategoryAndActiveTrue(Category category);

    // Buscar por nombre (búsqueda parcial)
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Buscar por rango de precios
    List<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice);

    // Buscar productos con stock disponible
    List<Product> findByStockGreaterThanAndActiveTrue(Integer minStock);

    // Productos más vendidos (custom query)
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.id DESC")
    List<Product> findLatestProducts();

    // Buscar por múltiples criterios
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "p.active = true")
    List<Product> findProductsByCriteria(@Param("name") String name,
                                         @Param("categoryId") Long categoryId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice);
}