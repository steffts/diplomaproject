package org.example.volunteerplatform.repository;

import org.example.volunteerplatform.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByAuthorIdAndEventId(Long authorId, Long eventId);

    @Query("select f from Feedback f join fetch f.author where f.event.id = :eventId order by f.createdAt desc")
    List<Feedback> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
