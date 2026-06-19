package com.example.bookstore.service.impl;

import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Author;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Editorial;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.EditorialRepository;
import com.example.bookstore.service.IBookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl implements IBookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final EditorialRepository editorialRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           EditorialRepository editorialRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.editorialRepository = editorialRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAllWithDetails().stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAllWithDetails(pageable)
                .map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return bookRepository.findByIdWithDetails(id)
                .map(bookMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro no encontrado con ID: %d".formatted(id)));
    }

    @Override
    @Transactional
    public BookResponse save(BookResponse dto) {
        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Autor no encontrado con ID: %d".formatted(dto.authorId())));

        Editorial editorial = editorialRepository.findById(dto.editorialId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Editorial no encontrada con ID: %d".formatted(dto.editorialId())));

        if (bookRepository.existsByTitleAndAuthorId(dto.title(), dto.authorId())) {
            throw new DuplicateResourceException(
                    "Ya existe un libro con el titulo '%s' para el autor con ID: %d"
                            .formatted(dto.title(), dto.authorId()));
        }

        Book book = new Book();
        book.setTitle(dto.title());
        book.setImageUrl(dto.imageUrl());
        book.setEditorial(editorial);
        book.setAuthor(author);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookResponse dto) {
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro no encontrado con ID: %d".formatted(id)));

        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Autor no encontrado con ID: %d".formatted(dto.authorId())));

        Editorial editorial = editorialRepository.findById(dto.editorialId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Editorial no encontrada con ID: %d".formatted(dto.editorialId())));

        book.setTitle(dto.title());
        book.setImageUrl(dto.imageUrl());
        book.setEditorial(editorial);
        book.setAuthor(author);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro no encontrado con ID: %d".formatted(id)));
        bookRepository.delete(book);
    }
}
