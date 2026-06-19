package com.example.bookstore.service;

import com.example.bookstore.dto.request.LoginRequest;
import com.example.bookstore.dto.request.RegisterRequest;
import com.example.bookstore.dto.request.RegisterStoreRequest;
import com.example.bookstore.dto.response.TokenResponse;
import com.example.bookstore.dto.response.UserSummaryResponse;
import com.example.bookstore.model.enums.Role;
import com.example.bookstore.model.enums.UserStatus;

import java.util.List;

public interface IUserService {

    TokenResponse register(RegisterRequest request);

    TokenResponse registerStore(RegisterStoreRequest request);

    TokenResponse login(LoginRequest request);

    void logout(String token);

    void changeUserRole(Long userId, Role role);

    void changeUserStatus(Long userId, UserStatus status);

    List<UserSummaryResponse> getPendingStoreApplications();
}
