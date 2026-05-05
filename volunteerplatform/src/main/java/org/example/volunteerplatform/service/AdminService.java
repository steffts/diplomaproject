package org.example.volunteerplatform.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.entity.UserStatus;
import org.example.volunteerplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Transactional
    public User updateUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        user.setStatus(newStatus);
        return userRepository.save(user);
    }
}
