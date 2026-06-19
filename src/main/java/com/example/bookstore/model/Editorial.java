package com.example.bookstore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "editorials")
@Getter @Setter
@NoArgsConstructor
public class Editorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String country;
}
