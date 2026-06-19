package com.example.bookstore.service.impl;

import com.example.bookstore.dto.response.StoreResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.StoreMapper;
import com.example.bookstore.model.Store;
import com.example.bookstore.model.User;
import com.example.bookstore.model.enums.Role;
import com.example.bookstore.repository.StoreRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.IStoreService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreServiceImpl implements IStoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreMapper storeMapper;

    public StoreServiceImpl(StoreRepository storeRepository,
                            UserRepository userRepository,
                            StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeMapper = storeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> findAll() {
        return storeRepository.findAllWithOwner().stream()
                .map(storeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse findById(Long id) {
        return storeRepository.findByIdWithOwner(id)
                .map(storeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libreria no encontrada con ID: %d".formatted(id)));
    }

    @Override
    @Transactional
    public StoreResponse save(StoreResponse dto) {
        if (storeRepository.existsByRuc(dto.ruc())) {
            throw new DuplicateResourceException(
                    "Ya existe una libreria con el RUC: " + dto.ruc());
        }
        if (storeRepository.existsByName(dto.name())) {
            throw new DuplicateResourceException(
                    "Ya existe una libreria con el nombre: " + dto.name());
        }

        Long ownerId = resolveOwnerId(dto.ownerId());
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: %d".formatted(ownerId)));

        Store store = new Store();
        store.setName(dto.name());
        store.setRuc(dto.ruc());
        store.setOwner(owner);
        store.setActive(dto.active());
        return storeMapper.toResponse(storeRepository.save(store));
    }

    // STORE: auto-asigna su propio ID y valida que no tenga tienda previa.
    // ADMIN: usa el ownerId del body (debe informarlo).
    private Long resolveOwnerId(Long dtoOwnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User current = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (current.getRole() == Role.STORE) {
            if (storeRepository.findByOwnerId(current.getId()).isPresent()) {
                throw new DuplicateResourceException("Ya tienes una libreria registrada en la plataforma");
            }
            return current.getId();
        }
        return dtoOwnerId;
    }

    @Override
    @Transactional
    public StoreResponse update(Long id, StoreResponse dto) {
        Store store = storeRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libreria no encontrada con ID: %d".formatted(id)));
        store.setName(dto.name());
        store.setRuc(dto.ruc());
        store.setActive(dto.active());
        return storeMapper.toResponse(storeRepository.save(store));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libreria no encontrada con ID: %d".formatted(id)));
        storeRepository.delete(store);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoreResponse> findByOwnerId(Long ownerId) {
        return storeRepository.findByOwnerId(ownerId).map(storeMapper::toResponse);
    }
}
