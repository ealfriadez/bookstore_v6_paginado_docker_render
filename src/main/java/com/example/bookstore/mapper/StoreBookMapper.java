package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.StoreBookResponse;
import com.example.bookstore.model.StoreBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Resuelve store + book
@Mapper(componentModel = "spring")
public interface StoreBookMapper {

    @Mapping(source = "store.id",        target = "storeId")
    @Mapping(source = "store.name",      target = "storeName")
    @Mapping(source = "book.id",         target = "bookId")
    @Mapping(source = "book.title",      target = "bookTitle")
    @Mapping(source = "book.imageUrl",   target = "bookImageUrl")
    StoreBookResponse toResponse(StoreBook storeBook);
}
