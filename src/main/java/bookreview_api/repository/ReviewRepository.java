package com.bookreview.api.repository;

import com.bookreview.api.model.Review;
import com.bookreview.api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    
    List<Review> findByBook(Book book);
    
    List<Review> findByUser_Id(String userId);
    
    // Get reviews sorted by createdAt (newest first)
    List<Review> findAllByOrderByCreatedAtDesc();
    
    // Get reviews sorted by createdAt (oldest first)
    List<Review> findAllByOrderByCreatedAtAsc();
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.book LEFT JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByUser_IdWithDetails(@Param("userId") String userId);
    
    void deleteByBook(Book book);
}