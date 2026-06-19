package com.example.bookstore.dto.response;

public record AuthorWithBookCountResponse(
    Long id,
    String firstName,
    String lastName,
    String nationality,
    Long bookCount
) {}
