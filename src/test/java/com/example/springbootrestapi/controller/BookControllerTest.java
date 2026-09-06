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
        Book harryPotter = new Book("Harry Potter and the Philosopher's Stone", "J. K. Rowling", 10.99);
        when(bookService.searchBooksByTitle(eq("harry"))).thenReturn(List.of(harryPotter));

        mockMvc.perform(get("/api/books/search").param("title", "harry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Harry Potter and the Philosopher's Stone"));
    }

    @Test
    void searchBooksByTitleIsCaseInsensitive() throws Exception {
        Book harryPotter = new Book("Harry Potter and the Philosopher's Stone", "J. K. Rowling", 10.99);
        when(bookService.searchBooksByTitle(eq("HARRY"))).thenReturn(List.of(harryPotter));

        mockMvc.perform(get("/api/books/search").param("title", "HARRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Harry Potter and the Philosopher's Stone"));
    }

    @Test
    void searchBooksByTitleReturnsEmptyListWhenNoMatches() throws Exception {
        when(bookService.searchBooksByTitle(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/books/search").param("title", "abcdefgh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchBooksByTitleRejectsEmptyTitle() throws Exception {
        mockMvc.perform(get("/api/books/search").param("title", ""))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).searchBooksByTitle(anyString());
    }

    @Test
    void searchBooksByTitleRejectsMissingTitleParameter() throws Exception {
        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).searchBooksByTitle(anyString());
    }
}
