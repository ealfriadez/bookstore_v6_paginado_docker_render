package com.example.bookstore.dto.response;

import com.example.bookstore.model.enums.Role;
import com.example.bookstore.model.enums.UserStatus;

public record TokenResponse(
    String     token,
    Long       id,
    String     email,
    Role       role,
    UserStatus status
) {}
