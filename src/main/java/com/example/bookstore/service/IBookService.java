package com.example.bookstore.service;

import com.example.bookstore.dto.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBookService extends CrudService<BookResponse, Long> {
    Page<BookResponse> findAll(Pageable pageable);
}
