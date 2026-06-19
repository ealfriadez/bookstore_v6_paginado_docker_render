package com.example.bookstore.service;

import com.example.bookstore.dto.response.BookSalesReportResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface IReportService {

    List<BookSalesReportResponse> getSalesByBook(LocalDateTime from, LocalDateTime to);

    SalesSummaryResponse getSalesSummary(LocalDateTime from, LocalDateTime to);
}
