package com.example.springbootrestapi.repository;

import com.example.springbootrestapi.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Case-insensitive partial title search
    List<Book> findByTitleContainingIgnoreCase(String title);
}
