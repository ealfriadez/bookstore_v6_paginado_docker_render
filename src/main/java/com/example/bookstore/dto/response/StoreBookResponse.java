package com.example.bookstore.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record StoreBookResponse(
    Long id,
    Long storeId,
    String storeName,
    Long bookId,
    String bookTitle,
    String bookImageUrl,

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    BigDecimal price,

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    Integer stock,

    boolean active
) {}
