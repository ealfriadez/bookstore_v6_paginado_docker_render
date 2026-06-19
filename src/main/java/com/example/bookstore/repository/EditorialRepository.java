package com.example.bookstore.repository;

import com.example.bookstore.model.Editorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EditorialRepository extends JpaRepository<Editorial, Long> {

    boolean existsByName(String name);

    Optional<Editorial> findByName(String name);
}
