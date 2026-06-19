package com.example.bookstore.controller;

import com.example.bookstore.dto.response.PagedResponse;
import com.example.bookstore.dto.response.StoreBookResponse;
import com.example.bookstore.service.IStoreBookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-books")
@Tag(name = "Inventario de librerias", description = "Gestion de precio y stock por libreria — requiere autenticacion")
public class StoreBookController {

    private final IStoreBookService storeBookService;

    public StoreBookController(IStoreBookService storeBookService) {
        this.storeBookService = storeBookService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @GetMapping
    public ResponseEntity<PagedResponse<StoreBookResponse>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(storeBookService.findAll(pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @GetMapping("/{id}")
    public ResponseEntity<StoreBookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storeBookService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @GetMapping("/by-store/{storeId}")
    public ResponseEntity<List<StoreBookResponse>> getByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeBookService.findByStoreId(storeId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @PostMapping
    public ResponseEntity<StoreBookResponse> create(@Valid @RequestBody StoreBookResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeBookService.save(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @PutMapping("/{id}")
    public ResponseEntity<StoreBookResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody StoreBookResponse request) {
        return ResponseEntity.ok(storeBookService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storeBookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
