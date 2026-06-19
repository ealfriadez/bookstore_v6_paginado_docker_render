package com.example.bookstore.dto.response;

import java.math.BigDecimal;

public record SalesReportByBookResponse(
    Long bookId,
    String bookTitle,
    Long totalQuantity,
    BigDecimal totalRevenue,
    Long totalPurchases
) {}
