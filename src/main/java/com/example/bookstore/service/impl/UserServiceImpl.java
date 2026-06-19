package com.example.bookstore.service.impl;

import com.example.bookstore.dto.request.LoginRequest;
import com.example.bookstore.dto.request.RegisterRequest;
import com.example.bookstore.dto.request.RegisterStoreRequest;
import com.example.bookstore.dto.response.TokenResponse;
import com.example.bookstore.dto.response.UserSummaryResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.model.User;
import com.example.bookstore.model.UserProfile;
import com.example.bookstore.model.enums.Role;
import com.example.bookstore.model.enums.UserStatus;
import com.example.bookstore.repository.UserProfileRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.TokenBlacklist;
import com.example.bookstore.security.TokenProvider;
import com.example.bookstore.service.IUserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository        userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider         tokenProvider;
    private final TokenBlacklist        tokenBlacklist;

    public UserServiceImpl(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           TokenProvider tokenProvider,
                           TokenBlacklist tokenBlacklist) {
        this.userRepository        = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder       = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider         = tokenProvider;
        this.tokenBlacklist        = tokenBlacklist;
    }

    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El email ya esta registrado");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Perfil vacío creado en la misma transacción
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        userProfileRepository.save(profile);

        return authenticate(request.email(), request.password());
    }

    @Override
    @Transactional
    public TokenResponse registerStore(RegisterStoreRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El email ya esta registrado");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        // PENDING: queda visible para el admin como solicitud de libreria pendiente
        user.setStatus(UserStatus.PENDING);
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        userProfileRepository.save(profile);

        return authenticate(request.email(), request.password());
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        return authenticate(request.email(), request.password());
    }

    @Override
    public void logout(String token) {
        tokenBlacklist.revoke(token);
    }

    @Override
    @Transactional
    public void changeUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: %d".formatted(userId)));
        user.setRole(role);
        // Al promover a STORE se activa la cuenta automaticamente
        if (role == Role.STORE) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: %d".formatted(userId)));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getPendingStoreApplications() {
        return userRepository.findByStatus(UserStatus.PENDING).stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getEmail(), u.getRole(), u.getStatus()))
                .toList();
    }

    private TokenResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        String token = tokenProvider.createToken(authentication);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + email));
        return new TokenResponse(token, user.getId(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
