package org.example.volunteerplatform.dto;

import org.example.volunteerplatform.entity.Role;

import java.util.List;

public class UserProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private List<EventSummaryDto> createdEvents;
    private List<EventSummaryDto> participatingEvents;

    // Inner DTO for event summary
    public static class EventSummaryDto {
        private Long id;
        private String title;

        public EventSummaryDto(Long id, String title) {
            this.id = id;
            this.title = title;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<EventSummaryDto> getCreatedEvents() { return createdEvents; }
    public void setCreatedEvents(List<EventSummaryDto> createdEvents) { this.createdEvents = createdEvents; }
    public List<EventSummaryDto> getParticipatingEvents() { return participatingEvents; }
    public void setParticipatingEvents(List<EventSummaryDto> participatingEvents) { this.participatingEvents = participatingEvents; }
}
