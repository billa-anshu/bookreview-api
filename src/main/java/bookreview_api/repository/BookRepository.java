package com.bookreview.api.repository;

import com.bookreview.api.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {
    
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
        String title, String author, Pageable pageable);
    
    Page<Book> findByGenre(String genre, Pageable pageable);
    
    Page<Book> findByAuthor(String author, Pageable pageable);
    
    Page<Book> findByGenreAndAuthor(String genre, String author, Pageable pageable);
    
    Page<Book> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);
}