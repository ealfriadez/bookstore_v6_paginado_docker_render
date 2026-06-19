package com.example.bookstore.service.impl;

import com.example.bookstore.dto.response.EditorialResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.EditorialMapper;
import com.example.bookstore.model.Editorial;
import com.example.bookstore.repository.EditorialRepository;
import com.example.bookstore.service.IEditorialService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EditorialServiceImpl implements IEditorialService {

    private final EditorialRepository editorialRepository;
    private final EditorialMapper editorialMapper;

    public EditorialServiceImpl(EditorialRepository editorialRepository,
                                EditorialMapper editorialMapper) {
        this.editorialRepository = editorialRepository;
        this.editorialMapper = editorialMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EditorialResponse> findAll() {
        return editorialRepository.findAll().stream()
                .map(editorialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EditorialResponse> findAll(Pageable pageable) {
        return editorialRepository.findAll(pageable)
                .map(editorialMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EditorialResponse findById(Long id) {
        return editorialRepository.findById(id)
                .map(editorialMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Editorial no encontrada con ID: %d".formatted(id)));
    }

    @Override
    @Transactional
    public EditorialResponse save(EditorialResponse dto) {
        if (editorialRepository.existsByName(dto.name())) {
            throw new DuplicateResourceException(
                    "Ya existe una editorial con el nombre: " + dto.name());
        }
        Editorial editorial = new Editorial();
        editorial.setName(dto.name());
        editorial.setCountry(dto.country());
        return editorialMapper.toResponse(editorialRepository.save(editorial));
    }

    @Override
    @Transactional
    public EditorialResponse update(Long id, EditorialResponse dto) {
        Editorial editorial = editorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Editorial no encontrada con ID: %d".formatted(id)));
        editorial.setName(dto.name());
        editorial.setCountry(dto.country());
        return editorialMapper.toResponse(editorialRepository.save(editorial));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Editorial editorial = editorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Editorial no encontrada con ID: %d".formatted(id)));
        editorialRepository.delete(editorial);
    }
}
