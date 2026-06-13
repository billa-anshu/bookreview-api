package com.bookreview.api.repository;

import com.bookreview.api.model.Review;
import com.bookreview.api.model.Book;
import com.bookreview.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    
    // Find reviews by book
    List<Review> findByBook(Book book);
    
    // Find reviews by user
    List<Review> findByUser(User user);
    
    // Find reviews by user ID (most efficient)
    List<Review> findByUserId(String userId);
    
    // Find reviews by user ID with book details (to avoid N+1 queries)
    @Query("SELECT r FROM Review r JOIN FETCH r.book WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByUserIdWithBooks(@Param("userId") String userId);
    
    // Find reviews by book with user details
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.book.id = :bookId ORDER BY r.createdAt DESC")
    List<Review> findByBookIdWithUsers(@Param("bookId") String bookId);
    
    // Get all reviews with book and user details
    @Query("SELECT r FROM Review r JOIN FETCH r.book JOIN FETCH r.user ORDER BY r.createdAt DESC")
    List<Review> findAllWithDetails();
    
    // Check if user already reviewed a book
    boolean existsByUserIdAndBookId(String userId, String bookId);
    
    // Delete all reviews for a book
    void deleteByBook(Book book);
    
    // Get reviews sorted by createdAt (newest first)
    List<Review> findAllByOrderByCreatedAtDesc();
    
    // Get reviews sorted by createdAt (oldest first)
    List<Review> findAllByOrderByCreatedAtAsc();
}
