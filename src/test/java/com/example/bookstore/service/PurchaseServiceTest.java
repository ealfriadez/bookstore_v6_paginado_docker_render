package com.example.bookstore.service;

import com.example.bookstore.dto.request.PurchaseItemRequest;
import com.example.bookstore.dto.request.PurchaseRequest;
import com.example.bookstore.dto.response.PurchaseResponse;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.PurchaseMapper;
import com.example.bookstore.model.*;
import com.example.bookstore.model.enums.PurchaseStatus;
import com.example.bookstore.model.enums.Role;
import com.example.bookstore.repository.PurchaseRepository;
import com.example.bookstore.repository.StoreBookRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock private PurchaseRepository  purchaseRepository;
    @Mock private StoreBookRepository storeBookRepository;
    @Mock private UserRepository      userRepository;
    @Mock private PurchaseMapper      purchaseMapper;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setRole(Role.CUSTOMER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private StoreBook createStoreBook(Long id, String title, double price, int stock) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);

        Store store = new Store();
        store.setId(1L);
        store.setName("Libreria Test");

        StoreBook storeBook = new StoreBook();
        storeBook.setId(id);
        storeBook.setBook(book);
        storeBook.setStore(store);
        storeBook.setPrice(BigDecimal.valueOf(price));
        storeBook.setStock(stock);
        storeBook.setActive(true);
        return storeBook;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREACION DE COMPRA
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-08: El cliente compra un libro y la compra queda en estado PENDING esperando pago")
    void purchaseWithStock() {
        StoreBook storeBook = createStoreBook(1L, "Clean Code", 39.99, 10);
        PurchaseRequest request = new PurchaseRequest(List.of(new PurchaseItemRequest(1L, 2)));

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(storeBookRepository.findAllById(List.of(1L))).thenReturn(List.of(storeBook));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(purchaseMapper.toResponse(any(Purchase.class))).thenReturn(
                new PurchaseResponse(1L, List.of(), BigDecimal.valueOf(79.98),
                        "PENDING", LocalDateTime.now())
        );

        PurchaseResponse result = purchaseService.createPurchase(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(storeBook.getStock()).isEqualTo(8);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    @DisplayName("CP-09: El cliente no puede comprar si el stock es insuficiente")
    void purchaseWithoutStock() {
        StoreBook storeBook = createStoreBook(1L, "Clean Code", 39.99, 5);
        PurchaseRequest request = new PurchaseRequest(List.of(new PurchaseItemRequest(1L, 15)));

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(storeBookRepository.findAllById(List.of(1L))).thenReturn(List.of(storeBook));

        assertThatThrownBy(() -> purchaseService.createPurchase(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("CP-10: El cliente compra varios libros y el total se calcula correctamente")
    void purchaseMultipleBooks() {
        StoreBook sb1 = createStoreBook(1L, "Clean Code", 40.00, 10);
        StoreBook sb2 = createStoreBook(2L, "Refactoring", 25.00, 10);
        PurchaseRequest request = new PurchaseRequest(List.of(
                new PurchaseItemRequest(1L, 2),
                new PurchaseItemRequest(2L, 1)
        ));

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(storeBookRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(sb1, sb2));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(purchaseMapper.toResponse(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            return new PurchaseResponse(1L, List.of(), p.getTotal(), "PENDING", LocalDateTime.now());
        });

        PurchaseResponse result = purchaseService.createPurchase(request);

        assertThat(result.total()).isEqualByComparingTo(BigDecimal.valueOf(105.00));
    }

    @Test
    @DisplayName("CP-11: El stock de la libreria se reduce despues de una compra exitosa")
    void purchaseReducesStock() {
        StoreBook storeBook = createStoreBook(1L, "Clean Code", 39.99, 10);
        PurchaseRequest request = new PurchaseRequest(List.of(new PurchaseItemRequest(1L, 3)));

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(storeBookRepository.findAllById(List.of(1L))).thenReturn(List.of(storeBook));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(purchaseMapper.toResponse(any(Purchase.class))).thenReturn(
                new PurchaseResponse(1L, List.of(), BigDecimal.ZERO, "PENDING", LocalDateTime.now())
        );

        purchaseService.createPurchase(request);

        assertThat(storeBook.getStock()).isEqualTo(7);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONFIRMACION DE PAGO
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-12: El cliente confirma el pago y la compra pasa a estado PAID")
    void confirmPaymentChangesPendingToPaid() {
        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setStatus(PurchaseStatus.PENDING);
        purchase.setTotal(BigDecimal.valueOf(79.98));

        when(purchaseRepository.findByIdWithItems(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(
                new PurchaseResponse(1L, List.of(), BigDecimal.valueOf(79.98),
                        "PAID", LocalDateTime.now())
        );

        PurchaseResponse result = purchaseService.confirmPayment(1L);

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PAID);
        assertThat(result.status()).isEqualTo("PAID");
        verify(purchaseRepository).save(purchase);
    }

    @Test
    @DisplayName("CP-13: No es posible confirmar el pago de una compra que ya fue pagada")
    void confirmPaymentAlreadyPaidThrowsException() {
        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setStatus(PurchaseStatus.PAID);
        when(purchaseRepository.findByIdWithItems(1L)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.confirmPayment(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CANCELACION DE COMPRA
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-14: El cliente cancela su compra PENDING y el stock se restaura")
    void cancelPendingPurchaseRestoresStock() {
        StoreBook storeBook = createStoreBook(1L, "Clean Code", 39.99, 8);

        PurchaseItem item = new PurchaseItem();
        item.setStoreBook(storeBook);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(39.99));
        item.setSubtotal(BigDecimal.valueOf(79.98));

        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setStatus(PurchaseStatus.PENDING);
        purchase.setTotal(BigDecimal.valueOf(79.98));
        purchase.getItems().add(item);

        when(purchaseRepository.findByIdWithItems(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(
                new PurchaseResponse(1L, List.of(), BigDecimal.valueOf(79.98),
                        "CANCELLED", LocalDateTime.now())
        );

        purchaseService.cancelPurchase(1L);

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.CANCELLED);
        assertThat(storeBook.getStock()).isEqualTo(10);
        verify(purchaseRepository).save(purchase);
    }

    @Test
    @DisplayName("CP-15: No es posible cancelar una compra que ya fue pagada")
    void cancelPaidPurchase() {
        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setStatus(PurchaseStatus.PAID);
        when(purchaseRepository.findByIdWithItems(1L)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.cancelPurchase(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("CP-16: No es posible cancelar una compra que no existe")
    void cancelPurchaseNotFound() {
        when(purchaseRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.cancelPurchase(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
