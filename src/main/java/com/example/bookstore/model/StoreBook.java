package com.example.bookstore.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "store_books",
       uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "book_id"}))
@Getter @Setter
@NoArgsConstructor
public class StoreBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean active = true;

    // Optimistic locking: evita condición de carrera en actualización de stock
    @Version
    private Long version;
}
