package com.bookreview.api.dto;

import com.bookreview.api.model.Book;
import com.bookreview.api.model.Review;
import com.bookreview.api.model.User;
import java.time.LocalDateTime;

public class ReviewWithBookDTO {
    private String reviewId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookDTO book;
    private UserDTO user;
    
    public ReviewWithBookDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        
        if (review.getBook() != null) {
            this.book = new BookDTO(review.getBook());
        }
        
        if (review.getUser() != null) {
            this.user = new UserDTO(review.getUser());
        }
    }
    
    // Getters
    public String getReviewId() { return reviewId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public BookDTO getBook() { return book; }
    public UserDTO getUser() { return user; }
    
    // Inner DTO classes
    public static class BookDTO {
        private String id;
        private String title;
        private String author;
        private String genre;
        private String coverImage;
        private Integer publishYear;
        private Double rating;
        private Integer reviewCount;
        
        public BookDTO(Book book) {
            this.id = book.getId();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.genre = book.getGenre();
            this.coverImage = book.getCoverImage();
            this.publishYear = book.getPublishYear();
            this.rating = book.getRating();
            this.reviewCount = book.getReviewCount();
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getGenre() { return genre; }
        public String getCoverImage() { return coverImage; }
        public Integer getPublishYear() { return publishYear; }
        public Double getRating() { return rating; }
        public Integer getReviewCount() { return reviewCount; }
    }
    
    public static class UserDTO {
        private String id;
        private String name;
        private String email;
        private String role;
        
        public UserDTO(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}