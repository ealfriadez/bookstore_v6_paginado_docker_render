package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.StoreResponse;
import com.example.bookstore.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Resuelve owner (User)
@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(source = "owner.id",    target = "ownerId")
    @Mapping(source = "owner.email", target = "ownerEmail")
    StoreResponse toResponse(Store store);
}
