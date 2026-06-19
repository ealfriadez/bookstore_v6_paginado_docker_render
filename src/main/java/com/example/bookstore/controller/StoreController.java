package com.example.bookstore.controller;

import com.example.bookstore.dto.response.StoreResponse;
import com.example.bookstore.service.IStoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@Tag(name = "Librerias", description = "Gestion de librerias registradas en la plataforma")
public class StoreController {

    private final IStoreService storeService;

    public StoreController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAll() {
        return ResponseEntity.ok(storeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @PostMapping
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.save(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody StoreResponse request) {
        return ResponseEntity.ok(storeService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
