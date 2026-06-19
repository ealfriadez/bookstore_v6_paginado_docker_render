package com.example.bookstore.controller;

import com.example.bookstore.dto.request.PurchaseRequest;
import com.example.bookstore.dto.response.PurchaseResponse;
import com.example.bookstore.dto.response.SalesReportByBookResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;
import com.example.bookstore.service.IPurchaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@Tag(name = "Compras", description = "Compra de libros con control de stock")
public class PurchaseController {

    private final IPurchaseService purchaseService;

    public PurchaseController(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseService.createPurchase(request));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseResponse>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.findById(id));
    }

    // Simula webhook de pasarela de pago. En produccion este endpoint seria llamado por el proveedor.
    @PostMapping("/{id}/pay")
    public ResponseEntity<PurchaseResponse> confirmPayment(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.confirmPayment(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponse> cancelPurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.cancelPurchase(id));
    }

    @GetMapping("/reports/by-book")
    public ResponseEntity<List<SalesReportByBookResponse>> getSalesReportByBook(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(purchaseService.getSalesReportByBook(from, to));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<SalesSummaryResponse> getSalesSummary(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(purchaseService.getSalesSummary(from, to));
    }
}
