package com.example.bookstore.dto.response;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String address,
        String email
) {}
