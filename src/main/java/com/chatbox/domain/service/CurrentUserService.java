package com.chatbox.domain.service;

import com.chatbox.domain.exception.ResourceNotFoundException;
import com.chatbox.domain.model.entity.User;
import com.chatbox.domain.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()|| "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // JWT subject = phone number
        String number = auth.getName();

        return userRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for principal: " + number));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}