package com.shaddai.demo.src.service;

import com.shaddai.demo.src.model.Product;
import com.shaddai.demo.src.model.Category;
import com.shaddai.demo.src.repository.ProductRepository;
import com.shaddai.demo.src.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Crear nuevo producto
    public Product createProduct(Product product) {
        // Validar que la categoría existe
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            product.setCategory(category);
        }

        // Validaciones básicas
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        if (product.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        return productRepository.save(product);
    }

    // Obtener todos los productos
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Obtener solo productos activos
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    // Obtener producto por ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Obtener productos por categoría
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    // Buscar productos por nombre
    public List<Product> searchProducts(String searchTerm) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm);
    }

    // Buscar productos por rango de precios
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice);
    }

    // Buscar productos con criterios múltiples
    public List<Product> searchProductsByCriteria(String name, Long categoryId,
                                                  BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findProductsByCriteria(name, categoryId, minPrice, maxPrice);
    }

    // Actualizar producto
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setActive(productDetails.getActive());

        // Actualizar categoría si se proporciona
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    // Actualizar stock (para cuando se realizan ventas)
    public Product updateStock(Long productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (newStock < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        product.setStock(newStock);
        return productRepository.save(product);
    }

    // Reducir stock (cuando se vende)
    public Product reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente");
        }

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    // Eliminar producto (lo marca como inactivo)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setActive(false);
        productRepository.save(product);
    }

    // Productos más recientes
    public List<Product> getLatestProducts() {
        return productRepository.findLatestProducts();
    }
}