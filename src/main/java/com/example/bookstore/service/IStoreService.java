package com.example.bookstore.service;

import com.example.bookstore.dto.response.StoreResponse;

import java.util.Optional;

public interface IStoreService extends CrudService<StoreResponse, Long> {

    Optional<StoreResponse> findByOwnerId(Long ownerId);
}
