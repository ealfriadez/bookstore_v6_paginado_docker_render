package com.example.bookstore.mapper;

import com.example.bookstore.dto.response.EditorialResponse;
import com.example.bookstore.model.Editorial;
import org.mapstruct.Mapper;

// Mapeo directo, sin relaciones
@Mapper(componentModel = "spring")
public interface EditorialMapper {

    EditorialResponse toResponse(Editorial editorial);
}
