package com.example.bookstore.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookResponse(
    Long id,

    @NotBlank(message = "El titulo es obligatorio")
    String title,

    @NotBlank(message = "La URL de imagen es obligatoria")
    String imageUrl,

    @NotNull(message = "El ID de la editorial es obligatorio")
    Long editorialId,

    String editorialName,

    @NotNull(message = "El ID del autor es obligatorio")
    Long authorId,

    String authorName
) {}
