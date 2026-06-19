package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByTitleAndAuthorId(String title, Long authorId);

    @Query("SELECT b FROM Book b JOIN FETCH b.author JOIN FETCH b.editorial")
    List<Book> findAllWithDetails();

    @Query(
        value = "SELECT b FROM Book b JOIN FETCH b.author JOIN FETCH b.editorial",
        countQuery = "SELECT COUNT(b) FROM Book b"
    )
    Page<Book> findAllWithDetails(Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.author JOIN FETCH b.editorial WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
}
