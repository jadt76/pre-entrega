package com.shaddai.demo.src.repository;

import com.shaddai.demo.src.model.Cart;
import com.shaddai.demo.src.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Buscar carrito por usuario
    Optional<Cart> findByUser(User user);

    // Buscar carrito por ID de usuario
    Optional<Cart> findByUserId(Long userId);
}