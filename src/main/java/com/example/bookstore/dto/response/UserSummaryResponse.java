package com.example.bookstore.dto.response;

import com.example.bookstore.model.enums.Role;
import com.example.bookstore.model.enums.UserStatus;

public record UserSummaryResponse(
        Long id,
        String email,
        Role role,
        UserStatus status
) {}
