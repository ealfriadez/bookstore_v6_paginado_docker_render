package com.example.bookstore.dto.response;

import java.math.BigDecimal;

public record PurchaseItemResponse(
    Long storeBookId,
    Long bookId,
    String bookTitle,
    Long storeId,
    String storeName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
