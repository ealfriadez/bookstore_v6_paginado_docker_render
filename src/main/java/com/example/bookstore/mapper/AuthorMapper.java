package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.AuthorResponse;
import com.example.bookstore.model.Author;
import org.mapstruct.Mapper;

// Mapeo directo, sin relaciones
@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorResponse toResponse(Author author);
}
