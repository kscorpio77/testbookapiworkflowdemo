package com.example.springbootrestapi.service;

import com.example.springbootrestapi.entity.Book;
import com.example.springbootrestapi.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Test
    void searchBooksByTitleDelegatesTrimmedTitleToRepository() {
        BookService bookService = new BookService(bookRepository);
        Book harryPotter = new Book("Harry Potter and the Philosopher's Stone", "J. K. Rowling", 10.99);
        when(bookRepository.findByTitleContainingIgnoreCase("harry")).thenReturn(List.of(harryPotter));

        List<Book> result = bookService.searchBooksByTitle(" harry ");

        assertThat(result).containsExactly(harryPotter);
    }

    @Test
    void searchBooksByTitleReturnsEmptyListWhenNoMatches() {
        BookService bookService = new BookService(bookRepository);
        when(bookRepository.findByTitleContainingIgnoreCase("abcdefgh")).thenReturn(Collections.emptyList());

        List<Book> result = bookService.searchBooksByTitle("abcdefgh");

        assertThat(result).isEmpty();
    }

    @Test
    void searchBooksByTitleRejectsNullTitle() {
        BookService bookService = new BookService(bookRepository);

        assertThatThrownBy(() -> bookService.searchBooksByTitle(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchBooksByTitleRejectsBlankTitle() {
        BookService bookService = new BookService(bookRepository);

        assertThatThrownBy(() -> bookService.searchBooksByTitle("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
