package com.bookreview.api.controller;

import com.bookreview.api.dto.ReviewWithBookDTO;
import com.bookreview.api.model.Book;
import com.bookreview.api.model.Review;
import com.bookreview.api.model.User;
import com.bookreview.api.repository.BookRepository;
import com.bookreview.api.repository.ReviewRepository;
import com.bookreview.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.hibernate.Hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void updateBookRating(String bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            List<Review> bookReviews = reviewRepository.findByBook(book);
            if (bookReviews.isEmpty()) {
                book.setRating(0.0);
                book.setReviewCount(0);
            } else {
                double avg = bookReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
                book.setRating(Math.round(avg * 10) / 10.0);
                book.setReviewCount(bookReviews.size());
            }
            bookRepository.save(book);
        }
    }

    // Get reviews by book ID - PUBLIC
    @GetMapping
    public List<Review> getReviewsByBookId(@RequestParam(value = "id", required = false) String bookId) {
        if (bookId == null) {
            return reviewRepository.findAll();
        }
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return List.of();
        }
        return reviewRepository.findByBook(book);
    }

    // Get all reviews - ADMIN only
    @GetMapping("/all")
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Get current user's own reviews - RETURN DTO
    @GetMapping("/my")
    public List<ReviewWithBookDTO> getMyReviews() {
        String currentUserId = getCurrentUserId();
        List<Review> myReviews = reviewRepository.findByUser_Id(currentUserId);
        
        // Force initialization of book details
        for (Review review : myReviews) {
            Hibernate.initialize(review.getBook());
            Hibernate.initialize(review.getUser());
        }
        
        // Convert to DTO
        return myReviews.stream()
            .map(ReviewWithBookDTO::new)
            .collect(Collectors.toList());
    }

    // Add a review
    @PostMapping
    public Map<String, String> addReview(@RequestBody Review review) {
        String currentUserId = getCurrentUserId();
        Map<String, String> response = new HashMap<>();
        
        Book book = bookRepository.findById(review.getBook().getId()).orElse(null);
        if (book == null) {
            response.put("error", "Book not found");
            return response;
        }
        
        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) {
            response.put("error", "User not found");
            return response;
        }
        
        Review newReview = new Review();
        newReview.setRating(review.getRating());
        newReview.setComment(review.getComment());
        newReview.setBook(book);
        newReview.setUser(user);
        
        reviewRepository.save(newReview);
        
        updateBookRating(book.getId());
        
        response.put("message", "Review added successfully");
        response.put("reviewId", newReview.getReviewId());
        return response;
    }

    // Delete a review
    @DeleteMapping("/{reviewId}")
    public Map<String, String> deleteReview(@PathVariable String reviewId) {
        String currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Map<String, String> response = new HashMap<>();
        
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            response.put("error", "Review not found");
            return response;
        }
        
        String bookId = review.getBook().getId();
        
        if (review.getUser().getId().equals(currentUserId) || 
            (currentUser != null && "ADMIN".equals(currentUser.getRole()))) {
            reviewRepository.delete(review);
            updateBookRating(bookId);
            response.put("message", "Review deleted successfully");
            return response;
        } else {
            response.put("error", "You can only delete your own reviews");
            return response;
        }
    }

    // Update a review
    @PutMapping("/{reviewId}")
    public Map<String, String> updateReview(@PathVariable String reviewId, @RequestBody Review updatedReview) {
        String currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Map<String, String> response = new HashMap<>();
        
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            response.put("error", "Review not found");
            return response;
        }
        
        String bookId = review.getBook().getId();
        
        if (review.getUser().getId().equals(currentUserId) || 
            (currentUser != null && "ADMIN".equals(currentUser.getRole()))) {
            review.setRating(updatedReview.getRating());
            review.setComment(updatedReview.getComment());
            reviewRepository.save(review);
            updateBookRating(bookId);
            response.put("message", "Review updated successfully");
            return response;
        } else {
            response.put("error", "You can only update your own reviews");
            return response;
        }
    }
}