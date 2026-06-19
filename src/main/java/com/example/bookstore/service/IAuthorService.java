package com.example.bookstore.service;

import com.example.bookstore.dto.response.AuthorResponse;
import com.example.bookstore.dto.response.AuthorWithBookCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAuthorService extends CrudService<AuthorResponse, Long> {

    Page<AuthorResponse> findAll(Pageable pageable);

    List<AuthorResponse> findByNationality(String nationality);

    List<AuthorResponse> searchByLastName(String lastName);

    List<AuthorWithBookCountResponse> findAuthorsOrderedByBookCount();
}
