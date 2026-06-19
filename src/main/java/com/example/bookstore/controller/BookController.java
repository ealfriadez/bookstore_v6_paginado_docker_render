package com.example.bookstore.controller;

import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.dto.response.PagedResponse;
import com.example.bookstore.service.IBookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/books")
@Tag(name = "Libros", description = "Gestion del catalogo global de libros — requiere autenticacion")
public class BookController {

    private final IBookService bookService;

    public BookController(IBookService bookService) {
        this.bookService = bookService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @GetMapping
    public ResponseEntity<PagedResponse<BookResponse>> getAll(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(bookService.findAll(pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.save(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody BookResponse request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
