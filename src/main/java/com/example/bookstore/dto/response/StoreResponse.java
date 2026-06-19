package com.example.bookstore.dto.response;

import jakarta.validation.constraints.NotBlank;

public record StoreResponse(
    Long id,

    @NotBlank(message = "El nombre de la libreria es obligatorio")
    String name,

    @NotBlank(message = "El RUC es obligatorio")
    String ruc,

    Long ownerId,
    String ownerEmail,
    boolean active
) {}
