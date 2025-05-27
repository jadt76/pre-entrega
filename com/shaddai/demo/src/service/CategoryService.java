package com.shaddai.demo.src.service;

import com.shaddai.demo.src.model.Category;
import com.shaddai.demo.src.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Crear nueva categoría
    public Category createCategory(Category category) {
        // Verificar si ya existe una categoría con ese nombre
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        return categoryRepository.save(category);
    }

    // Obtener todas las categorías
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Obtener categoría por ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Obtener categoría por nombre
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Actualizar categoría
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Verificar si el nuevo nombre ya existe (y no es la misma categoría)
        if (!category.getName().equals(categoryDetails.getName()) &&
                categoryRepository.existsByName(categoryDetails.getName())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());

        return categoryRepository.save(category);
    }

    // Eliminar categoría
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Verificar si tiene productos asociados
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoryRepository.deleteById(id);
    }

    // Buscar categorías por nombre parcial
    public List<Category> searchCategories(String searchTerm) {
        return categoryRepository.findByNameContainingIgnoreCase(searchTerm);
    }
}
