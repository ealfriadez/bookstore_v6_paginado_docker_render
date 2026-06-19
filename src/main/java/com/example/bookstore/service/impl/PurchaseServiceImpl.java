package com.example.bookstore.service.impl;

import com.example.bookstore.dto.request.PurchaseItemRequest;
import com.example.bookstore.dto.request.PurchaseRequest;
import com.example.bookstore.dto.response.PurchaseResponse;
import com.example.bookstore.dto.response.SalesReportByBookResponse;
import com.example.bookstore.dto.response.SalesSummaryResponse;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.PurchaseMapper;
import com.example.bookstore.model.Purchase;
import com.example.bookstore.model.PurchaseItem;
import com.example.bookstore.model.StoreBook;
import com.example.bookstore.model.User;
import com.example.bookstore.model.enums.PurchaseStatus;
import com.example.bookstore.repository.PurchaseRepository;
import com.example.bookstore.repository.StoreBookRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.IPurchaseService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PurchaseServiceImpl implements IPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final StoreBookRepository storeBookRepository;
    private final UserRepository userRepository;
    private final PurchaseMapper purchaseMapper;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                               StoreBookRepository storeBookRepository,
                               UserRepository userRepository,
                               PurchaseMapper purchaseMapper) {
        this.purchaseRepository = purchaseRepository;
        this.storeBookRepository = storeBookRepository;
        this.userRepository = userRepository;
        this.purchaseMapper = purchaseMapper;
    }

    @Override
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + email));

        List<Long> storeBookIds = request.items().stream()
                .map(PurchaseItemRequest::storeBookId)
                .toList();

        // Una sola query para todos los StoreBook (evita N+1)
        Map<Long, StoreBook> storeBookMap = storeBookRepository.findAllById(storeBookIds).stream()
                .collect(Collectors.toMap(StoreBook::getId, sb -> sb));

        for (Long storeBookId : storeBookIds) {
            if (!storeBookMap.containsKey(storeBookId)) {
                throw new ResourceNotFoundException(
                        "Libro de libreria no encontrado con ID: %d".formatted(storeBookId));
            }
        }

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        // PENDING: stock reservado, esperando confirmacion de pago
        purchase.setStatus(PurchaseStatus.PENDING);
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseItemRequest itemReq : request.items()) {
            StoreBook storeBook = storeBookMap.get(itemReq.storeBookId());

            if (storeBook.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para '%s' en '%s'. Disponible: %d, solicitado: %d"
                                .formatted(storeBook.getBook().getTitle(),
                                           storeBook.getStore().getName(),
                                           storeBook.getStock(), itemReq.quantity()));
            }

            storeBook.setStock(storeBook.getStock() - itemReq.quantity());

            BigDecimal subtotal = storeBook.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));

            PurchaseItem item = new PurchaseItem();
            item.setStoreBook(storeBook);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(storeBook.getPrice());
            item.setSubtotal(subtotal);
            item.setPurchase(purchase);

            purchase.getItems().add(item);
            total = total.add(subtotal);
        }

        purchase.setTotal(total);
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional
    public PurchaseResponse confirmPayment(Long id) {
        Purchase purchase = purchaseRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con ID: %d".formatted(id)));

        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Solo se puede confirmar el pago de una compra en estado PENDING. Estado actual: %s"
                            .formatted(purchase.getStatus()));
        }

        purchase.setStatus(PurchaseStatus.PAID);
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAllWithItems().stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse findById(Long id) {
        Purchase purchase = purchaseRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con ID: %d".formatted(id)));
        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional
    public PurchaseResponse cancelPurchase(Long id) {
        Purchase purchase = purchaseRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con ID: %d".formatted(id)));

        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Solo se puede cancelar una compra en estado PENDING. Estado actual: %s"
                            .formatted(purchase.getStatus()));
        }

        for (PurchaseItem item : purchase.getItems()) {
            StoreBook storeBook = item.getStoreBook();
            storeBook.setStock(storeBook.getStock() + item.getQuantity());
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesReportByBookResponse> getSalesReportByBook(LocalDate from, LocalDate to) {
        LocalDateTime dateFrom = from != null ? from.atStartOfDay() : null;
        LocalDateTime dateTo   = to   != null ? to.atTime(23, 59, 59) : null;

        return purchaseRepository.getSalesReportByBook(dateFrom, dateTo).stream()
                .map(row -> new SalesReportByBookResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3],
                        ((Number) row[4]).longValue()
                )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary(LocalDate from, LocalDate to) {
        LocalDateTime dateFrom = from != null ? from.atStartOfDay() : null;
        LocalDateTime dateTo   = to   != null ? to.atTime(23, 59, 59) : null;

        List<Object[]> result = purchaseRepository.getSalesSummary(dateFrom, dateTo);
        if (result.isEmpty()) {
            return new SalesSummaryResponse(BigDecimal.ZERO, 0L, BigDecimal.ZERO, null, null, null);
        }
        Object[] row = result.getFirst();
        return new SalesSummaryResponse(
                (BigDecimal) row[0],
                ((Number) row[1]).longValue(),
                (BigDecimal) row[2],
                row[3] != null ? ((Number) row[3]).longValue() : null,
                (String) row[4],
                row[5] != null ? ((Number) row[5]).longValue() : null
        );
    }
}
