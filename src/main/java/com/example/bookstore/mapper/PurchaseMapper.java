package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.PurchaseItemResponse;
import com.example.bookstore.dto.response.PurchaseResponse;
import com.example.bookstore.model.Purchase;
import com.example.bookstore.model.PurchaseItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// Resuelve items + storeBook
@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(source = "id.storeBookId",          target = "storeBookId")
    @Mapping(source = "storeBook.book.id",        target = "bookId")
    @Mapping(source = "storeBook.book.title",     target = "bookTitle")
    @Mapping(source = "storeBook.store.id",       target = "storeId")
    @Mapping(source = "storeBook.store.name",     target = "storeName")
    PurchaseItemResponse toItemResponse(PurchaseItem item);

    @Mapping(target = "items",  source = "items")
    @Mapping(target = "status", expression = "java(purchase.getStatus().name())")
    PurchaseResponse toResponse(Purchase purchase);

    List<PurchaseItemResponse> toItemResponseList(List<PurchaseItem> items);
}
