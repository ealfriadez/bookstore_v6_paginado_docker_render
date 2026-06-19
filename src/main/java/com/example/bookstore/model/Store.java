package com.example.bookstore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Getter @Setter
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String ruc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false)
    private boolean active = true;
}
