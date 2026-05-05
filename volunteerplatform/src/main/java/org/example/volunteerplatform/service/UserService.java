package org.example.volunteerplatform.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.volunteerplatform.dto.UpdateProfileRequest;
import org.example.volunteerplatform.dto.UserDto;
import org.example.volunteerplatform.dto.UserProfileDto;
import org.example.volunteerplatform.entity.Event;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.repository.EventRepository;
import org.example.volunteerplatform.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public UserService(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile() {
        User user = getCurrentUser();
        List<Event> createdEvents = eventRepository.findByOwner(user);
        List<Event> participatingEvents = eventRepository.findByParticipantsContains(user);
        return convertToUserProfileDto(user, createdEvents, participatingEvents);
    }

    @Transactional
    public UserDto updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        User updatedUser = userRepository.save(user);
        return convertToUserDto(updatedUser);
    }

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        // We should probably add status to UserDto as well
        return dto;
    }

    private UserProfileDto convertToUserProfileDto(User user, List<Event> createdEvents, List<Event> participatingEvents) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        dto.setCreatedEvents(createdEvents.stream()
                .map(event -> new UserProfileDto.EventSummaryDto(event.getId(), event.getTitle()))
                .collect(Collectors.toList()));

        dto.setParticipatingEvents(participatingEvents.stream()
                .map(event -> new UserProfileDto.EventSummaryDto(event.getId(), event.getTitle()))
                .collect(Collectors.toList()));

        return dto;
    }
}
