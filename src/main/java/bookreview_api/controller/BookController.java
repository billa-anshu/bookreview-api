package com.bookreview.api.controller;

import com.bookreview.api.model.Book;
import com.bookreview.api.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    // Get all books with pagination and sorting
    @GetMapping
    public Page<Book> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        // Valid sort fields: id, title, author, genre, publishYear, rating
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : 
                    Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findAll(pageable);
    }

    // Get book by ID
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable String id) {
        return bookRepository.findById(id).orElse(null);
    }

    // Search books
    @GetMapping("/search")
    public Page<Book> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
    }

    // Filter books
    @GetMapping("/filter")
    public Page<Book> filterBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (genre != null && author != null) {
            return bookRepository.findByGenreAndAuthor(genre, author, pageable);
        } else if (genre != null) {
            return bookRepository.findByGenre(genre, pageable);
        } else if (author != null) {
            return bookRepository.findByAuthor(author, pageable);
        } else if (minRating != null) {
            // Filter by minimum rating
            return bookRepository.findByRatingGreaterThanEqual((double) minRating, pageable);
        }
        
        return bookRepository.findAll(pageable);
    }
}