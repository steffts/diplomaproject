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
        return eventRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
        return convertToDto(event);
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
        newEvent.setParticipantLimit(request.getParticipantLimit());
        newEvent.setCategory(request.getCategory());
        newEvent.setImageUrl(request.getImageUrl());
        Event savedEvent = eventRepository.save(newEvent);
        return convertToDto(savedEvent);
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
        event.setParticipantLimit(request.getParticipantLimit());
        event.setCategory(request.getCategory());

        event.setImageUrl(request.getImageUrl());
        Event updatedEvent = eventRepository.save(event);
        return convertToDto(updatedEvent);
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
    public void addParticipant(Long eventId) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (event.getParticipantLimit() > 0 && event.getParticipants().size() >= event.getParticipantLimit()) {
            throw new IllegalStateException("Event is already full.");
        }

        if (event.getParticipants().contains(currentUser)) {
            throw new IllegalStateException("User is already a participant in this event.");
        }

        if (event.getOwner().equals(currentUser)) {
            throw new IllegalStateException("The owner cannot be a participant.");
        }

        event.getParticipants().add(currentUser);
        eventRepository.save(event);
    }

    @Transactional
    public void removeParticipant(Long eventId) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        if (!event.getParticipants().contains(currentUser)) {
            throw new IllegalStateException("User is not a participant in this event.");
        }

        event.getParticipants().remove(currentUser);
        eventRepository.save(event);
    }

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));
    }

    private boolean isOwnerOrAdmin(Event event, User user) {
        return user.getRole() == Role.ADMIN || event.getOwner().getId().equals(user.getId());
    }

    private EventDto convertToDto(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setEventDate(event.getEventDate());
        dto.setParticipantCount(event.getParticipants().size());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setCategory(event.getCategory());
        dto.setImageUrl(event.getImageUrl());
        User owner = event.getOwner();
        dto.setOwner(new EventDto.UserSummaryDto(owner.getId(), owner.getFirstName(), owner.getLastName()));

        // Conditionally add participants list
        User currentUser = null;
        try {
            currentUser = getCurrentUser();
        } catch (IllegalStateException e) {
            // User not logged in or not found, participants list will remain null
        }

        if (currentUser != null && (isOwnerOrAdmin(event, currentUser))) {
            dto.setParticipants(event.getParticipants().stream()
                    .map(p -> new EventDto.UserSummaryDto(p.getId(), p.getFirstName(), p.getLastName()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
