package com.example.bookstore.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorResponse(
    Long id,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    String firstName,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    String lastName,

    String nationality
) {}
