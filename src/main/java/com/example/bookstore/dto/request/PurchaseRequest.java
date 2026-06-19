package com.example.bookstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PurchaseRequest(
    @NotEmpty(message = "La compra debe tener al menos un libro")
    @Valid
    List<PurchaseItemRequest> items
) {}
