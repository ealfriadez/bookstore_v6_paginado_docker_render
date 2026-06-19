package com.example.bookstore.service;

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
import com.example.bookstore.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenProvider         tokenProvider;
    @Mock private TokenBlacklist        tokenBlacklist;

    @InjectMocks
    private UserServiceImpl userService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new User();
        savedUser.setId(5L);
        savedUser.setEmail("nuevo@test.com");
        savedUser.setRole(Role.CUSTOMER);
        savedUser.setStatus(UserStatus.ACTIVE);
    }

    private void mockAuthFlow(String email) {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.createToken(auth)).thenReturn("jwt-token");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRO DE CLIENTE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-17: El cliente se registra y su perfil queda creado automaticamente")
    void registerCustomerCreatesUserAndEmptyProfile() {
        RegisterRequest request = new RegisterRequest("nuevo@test.com", "Password@1");

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());
        mockAuthFlow("nuevo@test.com");

        TokenResponse result = userService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.role()).isEqualTo(Role.CUSTOMER);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("CP-18: No es posible registrarse con un correo que ya esta en uso")
    void registerDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("existente@test.com", "Password@1");
        when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
        verify(userProfileRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRO DE DUENO DE LIBRERIA
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-19: El dueno de libreria se registra con estado PENDING y perfil con nombre y telefono")
    void registerStoreOwnerCreatesProfileWithContactData() {
        RegisterStoreRequest request = new RegisterStoreRequest(
                "nuevo@test.com", "Password@1", "Carlos", "Quispe", "+51999888777"
        );

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@1")).thenReturn("hashed");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        when(userProfileRepository.save(profileCaptor.capture())).thenReturn(new UserProfile());

        // Para authenticate() devuelve savedUser con status PENDING
        savedUser.setStatus(UserStatus.PENDING);
        mockAuthFlow("nuevo@test.com");

        TokenResponse result = userService.registerStore(request);

        // Verifica que el usuario se guarda como PENDING
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.PENDING);

        // Verifica que el perfil lleva los datos de contacto
        UserProfile savedProfile = profileCaptor.getValue();
        assertThat(savedProfile.getFirstName()).isEqualTo("Carlos");
        assertThat(savedProfile.getLastName()).isEqualTo("Quispe");
        assertThat(savedProfile.getPhone()).isEqualTo("+51999888777");

        assertThat(result.role()).isEqualTo(Role.CUSTOMER);
        assertThat(result.status()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("CP-20: No es posible que una libreria se registre con un correo ya existente")
    void registerStoreDuplicateEmailThrowsException() {
        RegisterStoreRequest request = new RegisterStoreRequest(
                "existente@test.com", "Password@1", "Carlos", "Quispe", "+51999888777"
        );
        when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerStore(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
        verify(userProfileRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GESTION DE ROLES Y STATUS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-21: El admin aprueba a un usuario y lo convierte en dueno de libreria con estado ACTIVE")
    void adminPromotesUserToStore() {
        User user = new User();
        user.setId(5L);
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.PENDING);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changeUserRole(5L, Role.STORE);

        assertThat(user.getRole()).isEqualTo(Role.STORE);
        // Al promover a STORE el status pasa a ACTIVE automaticamente
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP-22: El admin no puede cambiar el rol de un usuario que no existe")
    void changeRoleUserNotFoundThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserRole(999L, Role.STORE))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP-23: El admin puede suspender una cuenta cambiando su estado a DISABLED")
    void adminDisablesUser() {
        User user = new User();
        user.setId(6L);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(6L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changeUserStatus(6L, UserStatus.DISABLED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP-24: El admin consulta solicitudes pendientes y obtiene los duenos de libreria en espera")
    void getPendingStoreApplications() {
        User pending = new User();
        pending.setId(7L);
        pending.setEmail("pendiente@test.com");
        pending.setRole(Role.CUSTOMER);
        pending.setStatus(UserStatus.PENDING);

        when(userRepository.findByStatus(UserStatus.PENDING)).thenReturn(List.of(pending));

        List<UserSummaryResponse> result = userService.getPendingStoreApplications();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(UserStatus.PENDING);
        assertThat(result.getFirst().email()).isEqualTo("pendiente@test.com");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP-25: El usuario cierra sesion y su token queda invalidado")
    void logoutRevokesToken() {
        userService.logout("jwt-token-valido");

        verify(tokenBlacklist).revoke("jwt-token-valido");
    }
}
