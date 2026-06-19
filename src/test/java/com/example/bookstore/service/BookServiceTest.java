package com.example.bookstore.service;

import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Author;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Editorial;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.EditorialRepository;
import com.example.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private EditorialRepository editorialRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Author author;
    private Editorial editorial;
    private Book book;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        author = new Author(1L, "Robert", "Martin", "American");

        editorial = new Editorial();
        editorial.setId(1L);
        editorial.setName("O'Reilly");
        editorial.setCountry("USA");

        book = new Book();
        book.setId(1L);
        book.setTitle("Clean Code");
        book.setImageUrl("http://img.com/clean.jpg");
        book.setEditorial(editorial);
        book.setAuthor(author);

        bookResponse = new BookResponse(
                1L, "Clean Code", "http://img.com/clean.jpg",
                1L, "O'Reilly", 1L, "Robert Martin"
        );
    }

    @Test
    @DisplayName("CP-01: Registrar libro con datos validos, autor y editorial existentes")
    void saveValidBook() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(editorialRepository.findById(1L)).thenReturn(Optional.of(editorial));
        when(bookRepository.existsByTitleAndAuthorId("Clean Code", 1L)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.save(bookResponse);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.authorName()).isEqualTo("Robert Martin");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("CP-02: Registrar libro con autor que no existe")
    void saveBookAuthorNotFound() {
        BookResponse request = new BookResponse(
                null, "Clean Code", "http://img.com/clean.jpg",
                1L, null, 999L, null
        );
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.save(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("CP-03: Registrar libro con editorial que no existe")
    void saveBookEditorialNotFound() {
        BookResponse request = new BookResponse(
                null, "Clean Code", "http://img.com/clean.jpg",
                999L, null, 1L, null
        );
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(editorialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.save(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("CP-04: Registrar libro con titulo duplicado para el mismo autor")
    void saveBookDuplicateTitle() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(editorialRepository.findById(1L)).thenReturn(Optional.of(editorial));
        when(bookRepository.existsByTitleAndAuthorId("Clean Code", 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.save(bookResponse))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("CP-05: Listar todos los libros del catalogo")
    void findAllBooks() {
        when(bookRepository.findAllWithDetails()).thenReturn(List.of(book));
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        List<BookResponse> result = bookService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("CP-06: Buscar libro por identificador existente")
    void findBookById() {
        when(bookRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.editorialName()).isEqualTo("O'Reilly");
    }

    @Test
    @DisplayName("CP-07: Buscar libro por identificador que no existe")
    void findBookByIdNotFound() {
        when(bookRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
