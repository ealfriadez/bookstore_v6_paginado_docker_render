package com.example.bookstore.service.impl;

import com.example.bookstore.dto.response.StoreBookResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.StoreBookMapper;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Store;
import com.example.bookstore.model.StoreBook;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.StoreBookRepository;
import com.example.bookstore.repository.StoreRepository;
import com.example.bookstore.service.IStoreBookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreBookServiceImpl implements IStoreBookService {

    private final StoreBookRepository storeBookRepository;
    private final StoreRepository storeRepository;
    private final BookRepository bookRepository;
    private final StoreBookMapper storeBookMapper;

    public StoreBookServiceImpl(StoreBookRepository storeBookRepository,
                                StoreRepository storeRepository,
                                BookRepository bookRepository,
                                StoreBookMapper storeBookMapper) {
        this.storeBookRepository = storeBookRepository;
        this.storeRepository = storeRepository;
        this.bookRepository = bookRepository;
        this.storeBookMapper = storeBookMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreBookResponse> findAll() {
        return storeBookRepository.findAllActive().stream()
                .map(storeBookMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreBookResponse> findAll(Pageable pageable) {
        return storeBookRepository.findAllActive(pageable)
                .map(storeBookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreBookResponse findById(Long id) {
        return storeBookRepository.findByIdWithDetails(id)
                .map(storeBookMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro de tienda no encontrado con ID: %d".formatted(id)));
    }

    @Override
    @Transactional
    public StoreBookResponse save(StoreBookResponse dto) {
        Store store = storeRepository.findById(dto.storeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libreria no encontrada con ID: %d".formatted(dto.storeId())));

        Book book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro no encontrado con ID: %d".formatted(dto.bookId())));

        if (storeBookRepository.existsByStoreIdAndBookId(dto.storeId(), dto.bookId())) {
            throw new DuplicateResourceException(
                    "La libreria ya tiene registrado ese libro en su catalogo");
        }

        StoreBook storeBook = new StoreBook();
        storeBook.setStore(store);
        storeBook.setBook(book);
        storeBook.setPrice(dto.price());
        storeBook.setStock(dto.stock());
        storeBook.setActive(dto.active());
        return storeBookMapper.toResponse(storeBookRepository.save(storeBook));
    }

    @Override
    @Transactional
    public StoreBookResponse update(Long id, StoreBookResponse dto) {
        StoreBook storeBook = storeBookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro de tienda no encontrado con ID: %d".formatted(id)));
        storeBook.setPrice(dto.price());
        storeBook.setStock(dto.stock());
        storeBook.setActive(dto.active());
        return storeBookMapper.toResponse(storeBookRepository.save(storeBook));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StoreBook storeBook = storeBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro de tienda no encontrado con ID: %d".formatted(id)));
        storeBookRepository.delete(storeBook);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreBookResponse> findByStoreId(Long storeId) {
        return storeBookRepository.findByStoreId(storeId).stream()
                .map(storeBookMapper::toResponse)
                .toList();
    }
}
