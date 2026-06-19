package com.example.bookstore.dto.response;

import jakarta.validation.constraints.NotBlank;

public record EditorialResponse(
    Long id,

    @NotBlank(message = "El nombre de la editorial es obligatorio")
    String name,

    String country
) {}
