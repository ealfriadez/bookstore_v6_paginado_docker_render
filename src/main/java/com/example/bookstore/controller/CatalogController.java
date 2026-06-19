package com.example.bookstore.controller;

import com.example.bookstore.dto.response.PagedResponse;
import com.example.bookstore.dto.response.StoreBookResponse;
import com.example.bookstore.service.IStoreBookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalogo", description = "Libros disponibles para comprar — acceso publico sin autenticacion")
public class CatalogController {

    private final IStoreBookService storeBookService;

    public CatalogController(IStoreBookService storeBookService) {
        this.storeBookService = storeBookService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<StoreBookResponse>> getAll(
            @PageableDefault(size = 12, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(storeBookService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreBookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storeBookService.findById(id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<StoreBookResponse>> getByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeBookService.findByStoreId(storeId));
    }
}
