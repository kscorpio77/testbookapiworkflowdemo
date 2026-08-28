package com.example.springbootrestapi;

import com.example.springbootrestapi.entity.Book;
import com.example.springbootrestapi.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SpringBootRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootRestApiApplication.class, args);
	}

	@Bean
	CommandLineRunner seedBooks(BookRepository bookRepository) {
		return args -> {
			if (bookRepository.count() == 0) {
				bookRepository.saveAll(List.of(
						new Book("Clean Code", "Robert C. Martin", 29.99),
						new Book("Effective Java", "Joshua Bloch", 34.99),
						new Book("Spring in Action", "Craig Walls", 39.99)
				));
			}
		};
	}

}
