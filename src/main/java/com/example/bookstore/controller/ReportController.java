package com.example.bookstore.controller;

import com.example.bookstore.dto.response.BookSalesReportResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;
import com.example.bookstore.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/sales-by-book")
    public ResponseEntity<List<BookSalesReportResponse>> salesByBook(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getSalesByBook(from, to));
    }

    @GetMapping("/summary")
    public ResponseEntity<SalesSummaryResponse> summary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getSalesSummary(from, to));
    }
}
