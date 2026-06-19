package com.example.bookstore.repository;

import com.example.bookstore.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AuthorRepository extends JpaRepository<Author, Long> {

    //QUERY METHOD
    List<Author> findByNationality(String nationality);

    //JPQL = Java Persistence Query Language
    @Query("""
           SELECT a FROM Author a
           WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%',:lastName,'%'))
        """)
    List<Author> searchByLastName(@Param("lastName") String lastName);

    @Query("""
           SELECT a FROM Author a
           WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%',:lastName,'%'))
        """)
    Page<Author> searchByLastName(@Param("lastName") String lastName, Pageable pageable);

    // SQL = Structured Query Language
    @Query(value = """
            SELECT a.id, a.first_name, a.last_name, a.nationality, COUNT(b.id) AS book_count
            FROM authors a
            LEFT JOIN books b ON a.id = b.author_id
            GROUP BY a.id, a.first_name, a.last_name, a.nationality
            ORDER BY COUNT(b.id) DESC
            """, nativeQuery = true)
    List<Object[]> findAuthorsOrderedByBookCount();
}
