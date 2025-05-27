package com.shaddai.demo.src.repository;

import com.shaddai.demo.src.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por email (para login)
    Optional<User> findByEmail(String email);

    // Verificar si existe un email
    boolean existsByEmail(String email);

    // Buscar usuarios por rol
    List<User> findByRole(User.Role role);

    // Buscar usuarios por nombre o apellido (búsqueda parcial)
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
}