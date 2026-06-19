package com.example.bookstore.controller;

import com.example.bookstore.dto.request.LoginRequest;
import com.example.bookstore.dto.request.RegisterRequest;
import com.example.bookstore.dto.request.RegisterStoreRequest;
import com.example.bookstore.dto.response.TokenResponse;
import com.example.bookstore.dto.response.UserSummaryResponse;
import com.example.bookstore.model.enums.Role;
import com.example.bookstore.model.enums.UserStatus;
import com.example.bookstore.service.IUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Login, registro y gestion de usuarios")
public class AuthController {

    private final IUserService userService;

    public AuthController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/register/store")
    public ResponseEntity<TokenResponse> registerStore(@Valid @RequestBody RegisterStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerStore(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            userService.logout(header.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    // Cambia el rol de un usuario. Al promover a STORE activa la cuenta automaticamente.
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestParam Role role) {
        userService.changeUserRole(id, role);
        return ResponseEntity.noContent().build();
    }

    // Activa o suspende una cuenta (ACTIVE / DISABLED). No permite PENDING manual.
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        userService.changeUserStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    // Lista los duenos de libreria que esperan aprobacion (status = PENDING).
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/pending-stores")
    public ResponseEntity<List<UserSummaryResponse>> getPendingStores() {
        return ResponseEntity.ok(userService.getPendingStoreApplications());
    }
}
