package org.example.volunteerplatform.repository;

import org.example.volunteerplatform.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("select m from ChatMessage m join fetch m.author where m.event.id = :eventId order by m.sentAt asc")
    List<ChatMessage> findByEventIdOrderBySentAt(Long eventId);
}
