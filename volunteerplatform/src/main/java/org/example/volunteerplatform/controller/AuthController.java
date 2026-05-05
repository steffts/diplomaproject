package org.example.volunteerplatform.controller;

import org.example.volunteerplatform.dto.LoginRequest;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.security.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            String message = authenticationService.register(user);
            return ResponseEntity.ok(message);
        } catch (IllegalStateException e) {
            // This will catch the "user already exists" error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        // The service layer will throw exceptions for bad credentials or locked accounts,
        // which will be handled by a global exception handler later.
        // For now, they might result in 500 errors, which is fine for debugging.
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
