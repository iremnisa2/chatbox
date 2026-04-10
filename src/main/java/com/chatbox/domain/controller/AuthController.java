package com.chatbox.domain.controller;

import com.chatbox.domain.model.entity.User;
import com.chatbox.domain.model.response.RegisterResponse;
import com.chatbox.domain.repository.UserRepository;
import com.chatbox.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req) {

        if (req.number() == null || req.number().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (userRepository.findByNumber(req.number()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User u = new User();
        u.setFirstname(req.firstname());
        u.setLastname(req.lastname());
        u.setNumber(req.number());
        u.setPasswordHash(passwordEncoder.encode(req.password()));

        userRepository.save(u);

        RegisterResponse response = new RegisterResponse(
                u.getId(),
                u.getFirstname(),
                u.getLastname(),
                u.getNumber(),
                u.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.number() == null || req.number().isBlank()) {
            return ResponseEntity.badRequest().body(new ApiError("NUMBER_REQUIRED"));
        }
        if (req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(new ApiError("PASSWORD_REQUIRED"));
        }

        User user = userRepository.findByNumber(req.number()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError("INVALID_CREDENTIALS"));
        }


        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError("INVALID_CREDENTIALS"));
        }

        String token = jwtUtil.generateToken(user.getNumber());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // DTOs
    public record RegisterRequest(String firstname, String lastname, String number, String password) {}
    public record LoginRequest(String number, String password) {}
    public record AuthResponse(String accessToken) {}
    public record ApiError(String error) {}
}