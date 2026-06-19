package com.example.bookstore.controller;

import com.example.bookstore.dto.request.UserProfileRequest;
import com.example.bookstore.dto.response.UserProfileResponse;
import com.example.bookstore.service.IUserProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Perfil", description = "Gestión del perfil personal del usuario autenticado")
public class UserProfileController {

    private final IUserProfileService userProfileService;

    public UserProfileController(IUserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userProfileService.getProfile(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> saveOrUpdateProfile(
            @Valid @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userProfileService.saveOrUpdate(userDetails.getUsername(), request));
    }
}
