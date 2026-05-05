package org.example.volunteerplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.volunteerplatform.entity.EventCategory;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // This will hide null fields (like participants) in the JSON response
public class EventDto {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDate;
    private UserSummaryDto owner;
    private int participantCount;
    private int participantLimit;
    private EventCategory category;
    private String imageUrl;
    private List<UserSummaryDto> participants; // New field

    public static class UserSummaryDto {
        private Long id;
        private String firstName;
        private String lastName;

        public UserSummaryDto() {}

        public UserSummaryDto(Long id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    // Getters and Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public UserSummaryDto getOwner() { return owner; }
    public void setOwner(UserSummaryDto owner) { this.owner = owner; }
    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }
    public int getParticipantLimit() { return participantLimit; }
    public void setParticipantLimit(int participantLimit) { this.participantLimit = participantLimit; }
    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<UserSummaryDto> getParticipants() { return participants; }
    public void setParticipants(List<UserSummaryDto> participants) { this.participants = participants; }
}
