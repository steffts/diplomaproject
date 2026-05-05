package org.example.volunteerplatform.dto;

import java.time.LocalDateTime;

public class FeedbackDto {

    private Long id;
    private int rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private EventDto.UserSummaryDto author;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public EventDto.UserSummaryDto getAuthor() { return author; }
    public void setAuthor(EventDto.UserSummaryDto author) { this.author = author; }
}
