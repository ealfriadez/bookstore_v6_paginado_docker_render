package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Resuelve author + editorial
@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "author.id",        target = "authorId")
    @Mapping(source = "editorial.id",     target = "editorialId")
    @Mapping(source = "editorial.name",   target = "editorialName")
    @Mapping(target = "authorName", expression = """
             java(book.getAuthor().getFirstName()
                  + " " + book.getAuthor().getLastName())
             """)
    BookResponse toResponse(Book book);
}
