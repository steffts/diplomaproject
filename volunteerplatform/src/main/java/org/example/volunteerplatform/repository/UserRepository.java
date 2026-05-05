package org.example.volunteerplatform.repository;

import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(UserStatus status);
}
