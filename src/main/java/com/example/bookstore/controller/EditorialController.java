package com.example.bookstore.controller;

import com.example.bookstore.dto.response.EditorialResponse;
import com.example.bookstore.dto.response.PagedResponse;
import com.example.bookstore.service.IEditorialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/editorials")
@Tag(name = "Editoriales", description = "Gestion de editoriales")
public class EditorialController {

    private final IEditorialService editorialService;

    public EditorialController(IEditorialService editorialService) {
        this.editorialService = editorialService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EditorialResponse>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(editorialService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EditorialResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(editorialService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EditorialResponse> create(@Valid @RequestBody EditorialResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(editorialService.save(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EditorialResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody EditorialResponse request) {
        return ResponseEntity.ok(editorialService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        editorialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
