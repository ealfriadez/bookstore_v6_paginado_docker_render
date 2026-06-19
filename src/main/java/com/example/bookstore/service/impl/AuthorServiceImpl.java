package com.example.bookstore.service.impl;

import com.example.bookstore.dto.response.AuthorResponse;
import com.example.bookstore.dto.response.AuthorWithBookCountResponse;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.AuthorMapper;
import com.example.bookstore.model.Author;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.service.IAuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorServiceImpl implements IAuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorResponse> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(authorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return authorRepository.findById(id)
                .map(authorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Autor no encontrado con ID: %d".formatted(id)));
    }

    @Override
    @Transactional
    public AuthorResponse save(AuthorResponse entity) {
        Author author = new Author();
        author.setFirstName(entity.firstName());
        author.setLastName(entity.lastName());
        author.setNationality(entity.nationality());
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Override
    @Transactional
    public AuthorResponse update(Long id, AuthorResponse entity) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Autor no encontrado con ID: %d".formatted(id)));
        author.setFirstName(entity.firstName());
        author.setLastName(entity.lastName());
        author.setNationality(entity.nationality());
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Autor no encontrado con ID: %d".formatted(id)));
        authorRepository.delete(author);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> findByNationality(String nationality) {
        return authorRepository.findByNationality(nationality).stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> searchByLastName(String lastName) {
        return authorRepository.searchByLastName(lastName).stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorWithBookCountResponse> findAuthorsOrderedByBookCount() {
        return authorRepository.findAuthorsOrderedByBookCount().stream()
                .map(row -> new AuthorWithBookCountResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue()))
                .toList();
    }
}
