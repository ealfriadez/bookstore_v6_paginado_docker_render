package com.example.bookstore.controller;

import com.example.bookstore.dto.response.AuthorResponse;
import com.example.bookstore.dto.response.AuthorWithBookCountResponse;
import com.example.bookstore.dto.response.PagedResponse;
import com.example.bookstore.service.IAuthorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Autores", description = "Gestion de autores")
public class AuthorController {

    private final IAuthorService authorService;

    public AuthorController(IAuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AuthorResponse>> getAllAuthors(
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(authorService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(
            @Valid @RequestBody AuthorResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authorService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorResponse request) {
        return ResponseEntity.ok(authorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-nationality")
    public ResponseEntity<List<AuthorResponse>> getByNationality(
            @RequestParam String nationality) {
        return ResponseEntity.ok(authorService.findByNationality(nationality));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AuthorResponse>> searchByLastName(
            @RequestParam String lastName) {
        return ResponseEntity.ok(authorService.searchByLastName(lastName));
    }

    @GetMapping("/by-book-count")
    public ResponseEntity<List<AuthorWithBookCountResponse>> getByBookCount() {
        return ResponseEntity.ok(authorService.findAuthorsOrderedByBookCount());
    }
}
