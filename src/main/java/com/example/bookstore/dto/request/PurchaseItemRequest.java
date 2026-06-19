package com.example.bookstore.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseItemRequest(
    @NotNull(message = "El ID del libro en la libreria es obligatorio")
    Long storeBookId,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    Integer quantity
) {}
