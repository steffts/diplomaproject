package org.example.volunteerplatform.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.volunteerplatform.dto.CreateEventRequest;
import org.example.volunteerplatform.dto.EventDto;
import org.example.volunteerplatform.dto.UpdateEventRequest;
import org.example.volunteerplatform.entity.Event;
import org.example.volunteerplatform.entity.Role;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.repository.EventRepository;
import org.example.volunteerplatform.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EventDto> getAllEvents() {
        User currentUser = currentUserOrNull();
        return eventRepository.findAllWithOwnerAndParticipants().stream()
                .map(event -> convertToDto(event, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
        User currentUser = currentUserOrNull();
        return convertToDto(event, currentUser);
    }

    @Transactional
    public EventDto createEvent(CreateEventRequest request) {
        User currentUser = getCurrentUser();
        Event newEvent = new Event();
        newEvent.setTitle(request.getTitle());
        newEvent.setDescription(request.getDescription());
        newEvent.setLocation(request.getLocation());
        newEvent.setEventDate(request.getEventDate());
        newEvent.setOwner(currentUser);
        newEvent.setParticipantLimit(toDbLimit(request.getParticipantLimit()));
        newEvent.setCategory(request.getCategory());
        newEvent.setImageUrl(request.getImageUrl());
        Event savedEvent = eventRepository.save(newEvent);
        return convertToDto(savedEvent, currentUser);
    }

    @Transactional
    public EventDto updateEvent(Long id, UpdateEventRequest request) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        if (!isOwnerOrAdmin(event, currentUser)) {
            throw new AccessDeniedException("You do not have permission to edit this event.");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setParticipantLimit(toDbLimit(request.getParticipantLimit()));
        event.setCategory(request.getCategory());
        event.setImageUrl(request.getImageUrl());
        Event updatedEvent = eventRepository.save(event);
        return convertToDto(updatedEvent, currentUser);
    }

    @Transactional
    public void deleteEvent(Long id) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        if (!isOwnerOrAdmin(event, currentUser)) {
            throw new AccessDeniedException("You do not have permission to delete this event.");
        }

        eventRepository.delete(event);
    }

    @Transactional
    public EventDto joinEvent(Long eventId) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        Integer limit = event.getParticipantLimit();
        if (limit != null && limit > 0 && event.getParticipants().size() >= limit) {
            throw new IllegalStateException("Event is already full.");
        }
        if (event.getParticipants().contains(currentUser)) {
            throw new IllegalStateException("User is already a participant in this event.");
        }
        if (event.getOwner().equals(currentUser)) {
            throw new IllegalStateException("The owner cannot join their own event as a participant.");
        }

        event.getParticipants().add(currentUser);
        Event saved = eventRepository.save(event);
        return convertToDto(saved, currentUser);
    }

    @Transactional
    public EventDto leaveEvent(Long eventId) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (event.getOwner().equals(currentUser)) {
            throw new IllegalStateException("Event organizer cannot leave their own event.");
        }
        if (!event.getParticipants().contains(currentUser)) {
            throw new IllegalStateException("You are not a participant of this event.");
        }

        event.getParticipants().remove(currentUser);
        Event saved = eventRepository.save(event);
        return convertToDto(saved, currentUser);
    }

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));
    }

    private User currentUserOrNull() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return null;
            }
            return userRepository.findByEmail(auth.getName()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isOwnerOrAdmin(Event event, User user) {
        return user.getRole() == Role.ADMIN || event.getOwner().getId().equals(user.getId());
    }

    /**
     * Converts the DTO's participantLimit (null = unlimited) to the DB value.
     * Storing 0 for "unlimited" keeps the NOT NULL constraint satisfied on databases
     * that haven't yet been migrated to allow NULL in this column.
     */
    private Integer toDbLimit(Integer requestedLimit) {
        return (requestedLimit == null || requestedLimit <= 0) ? 0 : requestedLimit;
    }

    /**
     * Converts the DB value back to the DTO convention: 0 or null → null (unlimited).
     */
    private Integer fromDbLimit(Integer dbLimit) {
        return (dbLimit == null || dbLimit <= 0) ? null : dbLimit;
    }

    private EventDto convertToDto(Event event, User currentUser) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setEventDate(event.getEventDate());
        dto.setParticipantCount(event.getParticipants().size());
        dto.setParticipantLimit(fromDbLimit(event.getParticipantLimit()));
        dto.setCategory(event.getCategory());
        dto.setImageUrl(event.getImageUrl());

        User owner = event.getOwner();
        dto.setOwner(new EventDto.UserSummaryDto(
                owner.getId(), owner.getFirstName(), owner.getLastName(),
                owner.getAverageRating(), owner.getRatingCount()
        ));

        if (currentUser != null) {
            boolean participating = event.getParticipants().stream()
                    .anyMatch(p -> p.getId().equals(currentUser.getId()));
            dto.setIsParticipant(participating);

            if (isOwnerOrAdmin(event, currentUser)) {
                dto.setParticipants(event.getParticipants().stream()
                        .map(p -> new EventDto.UserSummaryDto(p.getId(), p.getFirstName(), p.getLastName()))
                        .collect(Collectors.toList()));
            }
        }

        return dto;
    }
}
