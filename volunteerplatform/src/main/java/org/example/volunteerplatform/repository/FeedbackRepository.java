package org.example.volunteerplatform.repository;

import org.example.volunteerplatform.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
