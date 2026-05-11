package org.example.volunteerplatform.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.volunteerplatform.dto.ChatMessageDto;
import org.example.volunteerplatform.entity.ChatMessage;
import org.example.volunteerplatform.entity.Event;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.repository.ChatMessageRepository;
import org.example.volunteerplatform.repository.EventRepository;
import org.example.volunteerplatform.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       EventRepository eventRepository,
                       UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long eventId) {
        User current = getCurrentUser();
        Event event = loadEventAndCheckAccess(eventId, current);
        return chatMessageRepository.findByEventIdOrderBySentAt(eventId).stream()
                .map(m -> convertToDto(m, event.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDto sendMessage(Long eventId, String content) {
        User current = getCurrentUser();
        Event event = loadEventAndCheckAccess(eventId, current);

        ChatMessage message = new ChatMessage();
        message.setEvent(event);
        message.setAuthor(current);
        message.setContent(content);

        ChatMessage saved = chatMessageRepository.save(message);
        return convertToDto(saved, event.getOwner().getId());
    }

    private Event loadEventAndCheckAccess(Long eventId, User current) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        boolean isParticipant = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(current.getId()));
        boolean isOrganizer = event.getOwner().getId().equals(current.getId());

        if (!isParticipant && !isOrganizer) {
            throw new AccessDeniedException("Chat is only for event participants.");
        }
        return event;
    }

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));
    }

    private ChatMessageDto convertToDto(ChatMessage message, Long organizerId) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        User author = message.getAuthor();
        dto.setAuthorId(author.getId());
        dto.setAuthorName(author.getFirstName() + " " + author.getLastName());
        dto.setOrganizer(author.getId().equals(organizerId));
        return dto;
    }
}
