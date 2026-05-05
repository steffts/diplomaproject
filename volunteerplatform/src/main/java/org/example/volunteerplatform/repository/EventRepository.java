package org.example.volunteerplatform.repository;

import org.example.volunteerplatform.entity.Event;
import org.example.volunteerplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOwner(User owner);
    List<Event> findByParticipantsContains(User user);
}
