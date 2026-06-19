package com.example.bookstore.dto.response;

import java.math.BigDecimal;

public record BookSalesReportResponse(
        Long bookId,
        String bookTitle,
        Long storeId,
        String storeName,
        Long totalQuantity,
        BigDecimal totalRevenue,
        Long totalPurchases
) {}
