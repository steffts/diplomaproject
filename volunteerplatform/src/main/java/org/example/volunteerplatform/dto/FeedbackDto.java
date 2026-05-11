package org.example.volunteerplatform.dto;

import java.time.LocalDateTime;

public class FeedbackDto {

    private Long id;
    private int rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private EventDto.UserSummaryDto author;
    private OrganizerSummary organizer;

    public static class OrganizerSummary {
        private Long id;
        private Double averageRating;
        private Integer ratingCount;

        public OrganizerSummary() {}

        public OrganizerSummary(Long id, Double averageRating, Integer ratingCount) {
            this.id = id;
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
        public Integer getRatingCount() { return ratingCount; }
        public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    }

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
    public OrganizerSummary getOrganizer() { return organizer; }
    public void setOrganizer(OrganizerSummary organizer) { this.organizer = organizer; }
}
