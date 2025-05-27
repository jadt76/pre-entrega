package com.shaddai.demo.src.repository;

import com.shaddai.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Buscar categoría por nombre
    Optional<Category> findByName(String name);

    // Verificar si existe una categoría con ese nombre
    boolean existsByName(String name);

    // Buscar categorías por nombre parcial (para búsquedas)
    List<Category> findByNameContainingIgnoreCase(String name);
}