package com.example.bookstore.service.impl;

import com.example.bookstore.dto.request.UserProfileRequest;
import com.example.bookstore.dto.response.UserProfileResponse;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.UserProfileMapper;
import com.example.bookstore.model.User;
import com.example.bookstore.model.UserProfile;
import com.example.bookstore.repository.UserProfileRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.IUserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                                  UserRepository userRepository,
                                  UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Perfil no encontrado para el usuario: " + email));
        return userProfileMapper.toResponse(profile, email);
    }

    @Override
    @Transactional
    public UserProfileResponse saveOrUpdate(String email, UserProfileRequest request) {
        User user = getUserByEmail(email);

        // findById usa user_id como PK directamente (no necesita findByUserId)
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setAddress(request.address());

        return userProfileMapper.toResponse(userProfileRepository.save(profile), email);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con email: " + email));
    }
}
