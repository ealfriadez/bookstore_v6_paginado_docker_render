package com.example.bookstore.repository;

import com.example.bookstore.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByRuc(String ruc);

    boolean existsByName(String name);

    @Query("SELECT s FROM Store s JOIN FETCH s.owner WHERE s.id = :id")
    Optional<Store> findByIdWithOwner(@Param("id") Long id);

    @Query("SELECT s FROM Store s JOIN FETCH s.owner WHERE s.owner.id = :ownerId")
    Optional<Store> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT s FROM Store s JOIN FETCH s.owner")
    List<Store> findAllWithOwner();
}
