package com.example.bookstore.service;

import com.example.bookstore.dto.response.StoreBookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IStoreBookService extends CrudService<StoreBookResponse, Long> {

    Page<StoreBookResponse> findAll(Pageable pageable);

    List<StoreBookResponse> findByStoreId(Long storeId);
}
