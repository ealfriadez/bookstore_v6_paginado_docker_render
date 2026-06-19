package com.example.bookstore.repository;

import com.example.bookstore.model.StoreBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreBookRepository extends JpaRepository<StoreBook, Long> {

    boolean existsByStoreIdAndBookId(Long storeId, Long bookId);

    @Query("SELECT sb FROM StoreBook sb JOIN FETCH sb.store JOIN FETCH sb.book JOIN FETCH sb.book.author WHERE sb.id = :id")
    Optional<StoreBook> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT sb FROM StoreBook sb JOIN FETCH sb.store JOIN FETCH sb.book JOIN FETCH sb.book.author WHERE sb.store.id = :storeId")
    List<StoreBook> findByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT sb FROM StoreBook sb JOIN FETCH sb.store JOIN FETCH sb.book JOIN FETCH sb.book.author WHERE sb.active = true")
    List<StoreBook> findAllActive();

    @Query(
        value = "SELECT sb FROM StoreBook sb JOIN FETCH sb.store JOIN FETCH sb.book JOIN FETCH sb.book.author WHERE sb.active = true",
        countQuery = "SELECT COUNT(sb) FROM StoreBook sb WHERE sb.active = true"
    )
    Page<StoreBook> findAllActive(Pageable pageable);
}
