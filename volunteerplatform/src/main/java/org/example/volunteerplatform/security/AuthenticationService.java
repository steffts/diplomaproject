package org.example.volunteerplatform.security;

import org.example.volunteerplatform.dto.LoginRequest;
import org.example.volunteerplatform.entity.Role;
import org.example.volunteerplatform.entity.User;
import org.example.volunteerplatform.entity.UserStatus;
import org.example.volunteerplatform.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("A user with this email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // ВРЕМЕННИ ПРОМЕНИ ЗА СЪЗДАВАНЕ НА АДМИН
        /*user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        // ВРЕМЕННО ВРЪЩАМЕ ТОКЕН, ЗА ДА СЕ ЛОГНЕ ВЕДНАГА
        return jwtService.generateToken(user);*/

        user.setRole(Role.USER); // All new users are standard users
        user.setStatus(UserStatus.PENDING); // All new users must be approved

        userRepository.save(user);
        return "User registered successfully. Awaiting admin approval.";
    }

    public String login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (LockedException e) {
            throw new LockedException("User account is not active. Please wait for admin approval.");
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        return jwtService.generateToken(user);
    }
}
