package org.example.volunteerplatform.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.volunteerplatform.dto.CreateFeedbackRequest;
import org.example.volunteerplatform.dto.EventDto;
import org.example.volunteerplatform.dto.FeedbackDto;
import org.example.volunteerplatform.entity.Event;
import org.example.volunteerplatform.entity.Feedback;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.repository.EventRepository;
import org.example.volunteerplatform.repository.FeedbackRepository;
import org.example.volunteerplatform.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeedbackDto createFeedback(Long eventId, CreateFeedbackRequest request) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        // Validation logic
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot leave feedback for an event that has not yet occurred.");
        }
        if (!event.getParticipants().contains(currentUser)) {
            throw new IllegalStateException("Only participants can leave feedback.");
        }

        Feedback feedback = new Feedback();
        feedback.setRating(request.getRating());
        feedback.setReviewText(request.getReviewText());
        feedback.setAuthor(currentUser);
        feedback.setEvent(event);

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Update organizer's average rating
        updateOrganizerRating(event.getOwner(), request.getRating());

        return convertToDto(savedFeedback);
    }

    private void updateOrganizerRating(User organizer, int newRating) {
        double currentTotalRating = organizer.getAverageRating() * organizer.getRatingCount();
        int newRatingCount = organizer.getRatingCount() + 1;
        double newAverageRating = (currentTotalRating + newRating) / newRatingCount;

        organizer.setRatingCount(newRatingCount);
        organizer.setAverageRating(newAverageRating);
        userRepository.save(organizer);
    }

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));
    }

    private FeedbackDto convertToDto(Feedback feedback) {
        FeedbackDto dto = new FeedbackDto();
        dto.setId(feedback.getId());
        dto.setRating(feedback.getRating());
        dto.setReviewText(feedback.getReviewText());
        dto.setCreatedAt(feedback.getCreatedAt());
        User author = feedback.getAuthor();
        dto.setAuthor(new EventDto.UserSummaryDto(author.getId(), author.getFirstName(), author.getLastName()));
        return dto;
    }
}
