package com.example.bookstore.dto.response;

import java.math.BigDecimal;

public record SalesSummaryResponse(
        BigDecimal totalSales,
        Long totalPurchases,
        BigDecimal averagePerPurchase,
        Long bestSellerId,
        String bestSellerTitle,
        Long bestSellerQuantity
) {}
