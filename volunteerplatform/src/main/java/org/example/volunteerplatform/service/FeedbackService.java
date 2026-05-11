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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           EventRepository eventRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbacks(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
        return feedbackRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackDto createFeedback(Long eventId, CreateFeedbackRequest request) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Feedback can only be left after the event has ended.");
        }

        boolean isParticipant = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(currentUser.getId()));
        boolean isOrganizer = event.getOwner().getId().equals(currentUser.getId());
        if (!isParticipant && !isOrganizer) {
            throw new AccessDeniedException("Only participants or the organizer can leave feedback.");
        }

        if (feedbackRepository.existsByAuthorIdAndEventId(currentUser.getId(), eventId)) {
            throw new IllegalStateException("You have already reviewed this event.");
        }

        Feedback feedback = new Feedback();
        feedback.setRating(request.getRating());
        feedback.setReviewText(request.getReviewText());
        feedback.setAuthor(currentUser);
        feedback.setEvent(event);

        Feedback savedFeedback = feedbackRepository.save(feedback);

        User organizer = event.getOwner();
        updateOrganizerRating(organizer, request.getRating());

        FeedbackDto dto = convertToDto(savedFeedback);
        dto.setOrganizer(new FeedbackDto.OrganizerSummary(
                organizer.getId(),
                organizer.getAverageRating(),
                organizer.getRatingCount()
        ));
        return dto;
    }

    private void updateOrganizerRating(User organizer, int newRating) {
        double currentTotal = organizer.getAverageRating() * organizer.getRatingCount();
        int newCount = organizer.getRatingCount() + 1;
        double newAvg = Math.round(((currentTotal + newRating) / newCount) * 10.0) / 10.0;
        organizer.setRatingCount(newCount);
        organizer.setAverageRating(newAvg);
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
