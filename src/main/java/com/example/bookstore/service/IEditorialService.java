package com.example.bookstore.service;

import com.example.bookstore.dto.response.EditorialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IEditorialService extends CrudService<EditorialResponse, Long> {
    Page<EditorialResponse> findAll(Pageable pageable);
}
