package org.example.volunteerplatform.controller;

import org.example.volunteerplatform.dto.UserDto;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.entity.UserStatus;
import org.example.volunteerplatform.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsersByStatus(@RequestParam UserStatus status) {
        List<User> users = adminService.findUsersByStatus(status);
        List<UserDto> userDtos = users.stream().map(this::convertToUserDto).collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<UserDto> approveUser(@PathVariable Long id) {
        User updatedUser = adminService.updateUserStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok(convertToUserDto(updatedUser));
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<UserDto> suspendUser(@PathVariable Long id) {
        User updatedUser = adminService.updateUserStatus(id, UserStatus.INACTIVE);
        return ResponseEntity.ok(convertToUserDto(updatedUser));
    }

    @PostMapping("/users/{id}/activate")
    public ResponseEntity<UserDto> activateUser(@PathVariable Long id) {
        User updatedUser = adminService.updateUserStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok(convertToUserDto(updatedUser));
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
}
