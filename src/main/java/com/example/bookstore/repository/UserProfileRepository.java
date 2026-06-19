package com.example.bookstore.repository;

import com.example.bookstore.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

// findById(userId) y existsById(userId) heredados de JpaRepository
// son suficientes — user_id ES la clave primaria del perfil.
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
