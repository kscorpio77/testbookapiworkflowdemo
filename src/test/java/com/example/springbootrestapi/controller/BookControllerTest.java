package com.example.springbootrestapi.controller;

import com.example.springbootrestapi.entity.Book;
import com.example.springbootrestapi.repository.BookRepository;
import com.example.springbootrestapi.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @Test
    void createBookAcceptsPositivePrice() throws Exception {
        when(bookService.saveBook(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "author": "Robert Martin",
                                  "price": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25.0));

        verify(bookService).saveBook(any(Book.class));
    }

    @Test
    void createBookRejectsZeroPrice() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "author": "Robert Martin",
                                  "price": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("price must be greater than 0"));

        verify(bookService, never()).saveBook(any(Book.class));
    }

    @Test
    void createBookRejectsNegativePrice() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "author": "Robert Martin",
                                  "price": -25
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("price must be greater than 0"));

        verify(bookService, never()).saveBook(any(Book.class));
    }

    @Test
    void searchBooksByTitleReturnsMatchingBooks() throws Exception {
        Book harryPotter1 = new Book("Harry Potter and the Philosopher's Stone", "J. K. Rowling", 10.99);
        Book harryPotter2 = new Book("Harry Potter and the Chamber of Secrets", "J. K. Rowling", 11.99);
        when(bookService.searchBooksByTitle("harry")).thenReturn(List.of(harryPotter1, harryPotter2));

        mockMvc.perform(get("/api/books/search").param("title", "harry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Harry Potter and the Philosopher's Stone"))
                .andExpect(jsonPath("$[1].title").value("Harry Potter and the Chamber of Secrets"));
    }

    @Test
    void searchBooksByTitleSupportsPartialMatch() throws Exception {
        Book cleanCode = new Book("Clean Code", "Robert Martin", 29.99);
        when(bookService.searchBooksByTitle("clean")).thenReturn(List.of(cleanCode));

        mockMvc.perform(get("/api/books/search").param("title", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void searchBooksByTitleIsCaseInsensitiveLowercase() throws Exception {
        Book cleanCode = new Book("Clean Code", "Robert Martin", 29.99);
        when(bookService.searchBooksByTitle("harry")).thenReturn(List.of(cleanCode));

        mockMvc.perform(get("/api/books/search").param("title", "harry"))
                .andExpect(status().isOk());

        verify(bookService).searchBooksByTitle("harry");
    }

    @Test
    void searchBooksByTitleIsCaseInsensitiveUppercase() throws Exception {
        when(bookService.searchBooksByTitle("HARRY")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/books/search").param("title", "HARRY"))
                .andExpect(status().isOk());

        verify(bookService).searchBooksByTitle("HARRY");
    }

    @Test
    void searchBooksByTitleIsCaseInsensitiveMixedCase() throws Exception {
        when(bookService.searchBooksByTitle("HaRrY")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/books/search").param("title", "HaRrY"))
                .andExpect(status().isOk());

        verify(bookService).searchBooksByTitle("HaRrY");
    }

    @Test
    void searchBooksByTitleReturnsEmptyListWhenNoMatches() throws Exception {
        when(bookService.searchBooksByTitle("abcdefgh")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/books/search").param("title", "abcdefgh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchBooksByTitleRejectsEmptyTitle() throws Exception {
        when(bookService.searchBooksByTitle(eq(""))).thenThrow(new IllegalArgumentException("title must not be empty"));

        mockMvc.perform(get("/api/books/search").param("title", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title must not be empty"));
    }

    @Test
    void searchBooksByTitleRejectsMissingTitleParameter() throws Exception {
        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).searchBooksByTitle(anyString());
    }

    @Test
    void searchBooksByTitleIgnoresLeadingAndTrailingSpaces() throws Exception {
        when(bookService.searchBooksByTitle(" harry")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/books/search").param("title", " harry"))
                .andExpect(status().isOk());

        verify(bookService).searchBooksByTitle(" harry");
    }
}
