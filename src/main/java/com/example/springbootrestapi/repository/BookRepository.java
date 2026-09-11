package com.example.springbootrestapi.repository;

import com.example.springbootrestapi.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Case-insensitive partial title match used by the book title search feature (AI-5)
    List<Book> findByTitleContainingIgnoreCase(String title);
}
