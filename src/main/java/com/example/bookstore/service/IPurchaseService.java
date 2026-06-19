package com.example.bookstore.service;

import com.example.bookstore.dto.request.PurchaseRequest;
import com.example.bookstore.dto.response.PurchaseResponse;
import com.example.bookstore.dto.response.SalesReportByBookResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;

import java.time.LocalDate;
import java.util.List;

// No extiende CrudService: una compra no es un CRUD genérico.
// No se puede "guardar" como un Author, no se "actualiza" como un Book,
// y no se debería borrar (es un registro permanente).
// Forzar esos métodos violaría el Principio de Sustitución de Liskov.
public interface IPurchaseService {

    PurchaseResponse createPurchase(PurchaseRequest request);

    // Simula confirmacion de pago. En produccion sera invocado por el webhook de la pasarela.
    PurchaseResponse confirmPayment(Long id);

    List<PurchaseResponse> findAll();

    PurchaseResponse findById(Long id);

    // Solo se puede cancelar si la compra esta en estado PENDING (antes del pago).
    PurchaseResponse cancelPurchase(Long id);

    List<SalesReportByBookResponse> getSalesReportByBook(LocalDate from, LocalDate to);

    SalesSummaryResponse getSalesSummary(LocalDate from, LocalDate to);
}
