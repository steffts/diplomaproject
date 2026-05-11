package org.example.volunteerplatform.controller;

import org.example.volunteerplatform.dto.UserDto;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.entity.UserStatus;
import org.example.volunteerplatform.service.AdminService;
import org.example.volunteerplatform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsersByStatus(@RequestParam UserStatus status) {
        List<User> users = adminService.findUsersByStatus(status);
        List<UserDto> userDtos = users.stream().map(userService::convertToUserDto).collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PostMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        User updatedUser = adminService.updateUserStatus(id, status);
        return ResponseEntity.ok(userService.convertToUserDto(updatedUser));
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<UserDto> suspendUser(@PathVariable Long id) {
        User updatedUser = adminService.updateUserStatus(id, UserStatus.INACTIVE);
        return ResponseEntity.ok(userService.convertToUserDto(updatedUser));
    }
}
