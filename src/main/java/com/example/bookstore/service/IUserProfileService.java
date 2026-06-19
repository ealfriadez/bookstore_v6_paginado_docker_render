package com.example.bookstore.service;

import com.example.bookstore.dto.request.UserProfileRequest;
import com.example.bookstore.dto.response.UserProfileResponse;

public interface IUserProfileService {

    UserProfileResponse getProfile(String email);

    UserProfileResponse saveOrUpdate(String email, UserProfileRequest request);
}
